package com.recoded.taqadam.models.db;

import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.recoded.taqadam.models.Answer;
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
            DESC = "description",
            DATE_CREATED = "date_created",
            DATE_EXPIRES = "date_expires",
            TYPE = "type",
            ATTEMPTS = "attempted_by",
            COMPLETED_ATTEMPTS = "completed_by",
            OPTIONS = "options",
            JOB_ID = "job_id",
            TASK_IMAGE = "task_image";

    private String mUid;
    private static TaskDbHandler handler;
    private DatabaseReference mTasksDbRef;
    private DatabaseReference mAnswersDbRef;
    private Map<String, Task> tasksCache;

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

                    if (!task.getCompletedBy().containsValue(mUid)) {
                        tasksCache.put(ids[finalI], task);
                        tasks.add(task);
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

    public Task getTask(String taskId) {
        return tasksCache.get(taskId);
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
                mAnswersDbRef.child(taskId).child(id).setValue(a.toMap());
            }
        }
    }

    public void completeTask(String taskId) {
        if (tasksCache.containsKey(taskId)) {
            if (tasksCache.get(taskId).answer != null) {
                Answer a = tasksCache.get(taskId).answer;
                if (a.getAnswerId() != null && a.isCompleted()) {
                    mTasksDbRef.child(taskId).child(ATTEMPTS).child(mUid).setValue(null);
                    mTasksDbRef.child(taskId).child(COMPLETED_ATTEMPTS).child(mUid).setValue(a.getAnswerId());
                    mAnswersDbRef.child(taskId).child(a.getAnswerId()).setValue(a.toMap());

                    //add it to cache as well
                    tasksCache.get(taskId).addComplete(mUid);
                }
            }
        }
    }
}
