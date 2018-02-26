package com.recoded.taqadam.models.db;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.recoded.taqadam.models.Answer;
import com.recoded.taqadam.models.Job;
import com.recoded.taqadam.models.Task;
import com.recoded.taqadam.models.auth.UserAuthHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wisam on Jan 15 18.
 */

//This class handles both Tasks and Answers
public class TaskDbHandler {
    public static final String
            ATTEMPTS = "attempted_by",
            COMPLETED_ATTEMPTS = "completed_by",
            IMPRESSIONS = "impressions",
            JOB_ID = "job_id",
            TASK_IMAGE = "task_image";

    private String mUid;
    private static TaskDbHandler handler;
    private DatabaseReference mTasksDbRef;
    private DatabaseReference mAnswersDbRef;
    private OnImpressionsReachedListener impressionsListener;
    private Map<String, Task> tasksCache;

    private Map<String, ValueEventListener> completedByRefs;

    public synchronized static TaskDbHandler getInstance() {
        if (handler == null) {
            handler = new TaskDbHandler();
        }
        return handler;
    }

    private TaskDbHandler() {
        mUid = UserAuthHandler.getInstance().getUid();
        this.mTasksDbRef = FirebaseDatabase.getInstance().getReference()
                .child("Temp").child("Tasks");
        this.mAnswersDbRef = FirebaseDatabase.getInstance().getReference()
                .child("Temp").child("Answers");
        tasksCache = new HashMap<>();
        completedByRefs = new HashMap<>();
    }

    public com.google.android.gms.tasks.Task<List<Task>> getTasks(final String... ids) {
        final TaskCompletionSource<List<Task>> src = new TaskCompletionSource<>();
        DatabaseReference taskRef;
        final List<Task> tasks = new ArrayList<>();

        for (int i = 0; i < ids.length; i++) {
            //If tasks are cached then just return from cache
            if (tasksCache.containsKey(ids[i])) {
                tasks.add(tasksCache.get(ids[i]));
                if (i == ids.length - 1) {
                    src.setResult(tasks);
                }
                continue;
            }

            //otherwise get from db
            taskRef = mTasksDbRef.child(ids[i]);
            final int finalI = i;
            taskRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Task task = new Task(ids[finalI])
                            .fromMap((Map<String, Object>) dataSnapshot.getValue());
                    final Job j = JobDbHandler.getInstance().getJob(task.getJobId());
                    if (task.getCompletedBy().size() < j.getNoOfImpressions()) {
                        if (!task.getCompletedBy().containsValue(mUid)) {
                            tasksCache.put(ids[finalI], task);
                            tasks.add(task);
                            listenForImpressions(ids[finalI]);
                        }
                    }
                    if (finalI == ids.length - 1) {
                        src.setResult(tasks);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //Some tasks will send this because of set rules
                    if (finalI == ids.length - 1) {
                        src.setResult(tasks);
                    }
                }
            });
        }

        return src.getTask();
    }

    public void setImpressionsListener(OnImpressionsReachedListener listener) {
        impressionsListener = listener;
    }

    private void listenForImpressions(final String taskId) {
        final DatabaseReference ref = mTasksDbRef.child(taskId).child(COMPLETED_ATTEMPTS);
        final Job j = JobDbHandler.getInstance().getJob(getTask(taskId).getJobId());
        ValueEventListener l = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot.getValue()) != null) {
                    if (tasksCache.containsKey(taskId)) {
                        HashMap<String, Object> ids = ((HashMap<String, Object>) dataSnapshot.getValue());
                        if (j.getNoOfImpressions() <= ids.size()) {
                            if (impressionsListener != null) {
                                impressionsListener.onImpressionsReached(taskId);
                            }
                            tasksCache.remove(taskId);
                            ref.removeEventListener(this);
                            completedByRefs.remove(taskId);
                        }
                    }
                } else {
                    ref.removeEventListener(this);
                    completedByRefs.remove(taskId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ref.addValueEventListener(l);
        completedByRefs.put(taskId, l);
    }

    public Task getTask(String taskId) {
        return tasksCache.get(taskId);
    }

    public void removeTask(String taskId) {
        if (tasksCache.containsKey(taskId)) {
            tasksCache.remove(taskId);
        }
    }

    public com.google.android.gms.tasks.Task<Answer> getAnswer(final String taskId, final String answerId) {
        final TaskCompletionSource<Answer> src = new TaskCompletionSource<>();
        mAnswersDbRef.child(taskId).child(answerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Answer a = new Answer(taskId, answerId).fromMap((Map<String, Object>) dataSnapshot.getValue());
                src.setResult(a);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                src.setException(databaseError.toException());
            }
        });

        return src.getTask();
    }

    public void attemptTask(String taskId) {
        if (tasksCache.containsKey(taskId)) {
            if (tasksCache.get(taskId).answer != null) {
                Answer a = tasksCache.get(taskId).answer;
                boolean alreadyAttempted = a.getAnswerId() != null;
                String id = alreadyAttempted ? a.getAnswerId() : mAnswersDbRef.child(taskId).push().getKey();

                if (!alreadyAttempted) {
                    mTasksDbRef.child(taskId).child(ATTEMPTS).child(mUid).setValue(id);
                    a.setAnswerId(id);

                    //add it to cache as well
                    tasksCache.get(taskId).addAttempt(mUid, id);
                }

                a.setUserId(mUid);
                if (a.isCompleted()) a.setCompleted(false);
                mAnswersDbRef.child(taskId).child(id).setValue(a.toMap());
            }
        }
    }

    public void completeTask(final String taskId) {
        if (tasksCache.containsKey(taskId)) {
            if (tasksCache.get(taskId).answer != null) {
                final Answer a = tasksCache.get(taskId).answer;
                if (a.getAnswerId() != null && a.isCompleted()) {
                    mTasksDbRef.child(taskId).child(ATTEMPTS).child(mUid).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mTasksDbRef.child(taskId).child(COMPLETED_ATTEMPTS).child(mUid).setValue(a.getAnswerId()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mAnswersDbRef.child(taskId).child(a.getAnswerId()).setValue(a.toMap()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            tasksCache.remove(taskId);
                                            DatabaseReference attemptedRef = mTasksDbRef.child(taskId).child(IMPRESSIONS);
                                            attemptedRef.runTransaction(new Transaction.Handler() {
                                                @Override
                                                public Transaction.Result doTransaction(MutableData mutableData) {
                                                    int attempts = 0;
                                                    if (mutableData.getValue() != null) {
                                                        attempts = ((Long) mutableData.getValue()).intValue();
                                                    }
                                                    mutableData.setValue(++attempts);
                                                    return Transaction.success(mutableData);
                                                }

                                                @Override
                                                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }
        }
    }

    public void release() {
        //for logging out
        this.mUid = null;
        for (String k : completedByRefs.keySet()) {
            mTasksDbRef.child(k).removeEventListener(completedByRefs.get(k));
        }
        this.tasksCache.clear();
        handler = null;
    }

    public interface OnImpressionsReachedListener {
        void onImpressionsReached(String taskId);
    }

    public interface OnTaskCompletedListener {
        void onTaskCompleted(String taskId);
    }
}
