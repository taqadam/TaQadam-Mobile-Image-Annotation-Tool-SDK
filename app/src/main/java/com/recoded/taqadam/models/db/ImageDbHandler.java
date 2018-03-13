package com.recoded.taqadam.models.db;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.recoded.taqadam.models.Answer;
import com.recoded.taqadam.models.Image;
import com.recoded.taqadam.models.Job;
import com.recoded.taqadam.models.auth.UserAuthHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wisam on Jan 15 18.
 */

//This class handles both Tasks and Answers
public class ImageDbHandler {
    public static final String
            TASK_IMAGE = "task_image",
            SKIPPED_BY = "skipped_by",
            COMPLETED_BY = "completed_by";

    private String mUid;
    private static ImageDbHandler handler;
    private DatabaseReference mImagesDbRef;
    private DatabaseReference mAnswersDbRef;
    private OnImpressionsReachedListener impressionsReachedListener;
    private HashMap<String, ValueEventListener> listeners;

    public synchronized static ImageDbHandler getInstance() {
        if (handler == null) {
            handler = new ImageDbHandler();
        }
        return handler;
    }

    private ImageDbHandler() {
        mUid = UserAuthHandler.getInstance().getUid();
        this.mImagesDbRef = FirebaseDatabase.getInstance().getReference()
                .child("Work").child("Tasks");
        this.mAnswersDbRef = FirebaseDatabase.getInstance().getReference()
                .child("Work").child("Answers");
        listeners = new HashMap<>();
    }

    public Task<List<Image>> getTasks(final String jobId) {
        final TaskCompletionSource<List<Image>> src = new TaskCompletionSource<>();
        mImagesDbRef.child(jobId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                JobDbHandler.getInstance().getJob(jobId).setImages((Map<String, Map<String, Object>>) dataSnapshot.getValue());
                listenForImpressions(jobId);
                src.setResult(JobDbHandler.getInstance().getJob(jobId).getImagesList());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                src.setException(databaseError.toException());
            }
        });
        return src.getTask();
    }

    private void listenForImpressions(String jobId) {
        final Job job = JobDbHandler.getInstance().getJob(jobId);
        for (final Image img : job.getImagesList()) {
            ValueEventListener l = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getChildrenCount() >= job.getNoOfImpressions()) {
                        //job.removeImage(img.id);
                        mAnswersDbRef.child(img.id).removeEventListener(this);
                        listeners.remove(img.id);
                        impressionsReachedListener.onImpressionsReached(img.id);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mAnswersDbRef.child(img.id).addValueEventListener(l);
            listeners.put(img.id, l);
        }
    }

    public void setImpressionsReachedListener(OnImpressionsReachedListener listener) {
        impressionsReachedListener = listener;
    }

    public boolean submitAnswer(Answer answer) {
        if (answer.getRawAnswerData() != null && !answer.getRawAnswerData().isEmpty()) {
            final String jobId = answer.getJobId();
            final Image img = answer.getImage();
            mAnswersDbRef.child(img.id).child(mUid).setValue(answer.toMap()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mImagesDbRef.child(jobId).child(img.id).child(COMPLETED_BY).child(mUid).setValue(true)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    if (img.skipped)
                                        mImagesDbRef.child(jobId).child(img.id).child(SKIPPED_BY).child(mUid).removeValue();
                                }
                            });

                }
            });

            return true;
        }
        return false;
    }

    public void skipImage(String jobId, Image image) {
        if (jobId != null && image != null && image.id != null && !image.skipped) {
            mImagesDbRef.child(jobId).child(image.id).child(SKIPPED_BY).child(mUid).setValue(true);
        }
    }

    public void release() {
        //for logging out
        this.mUid = null;
        for (String id : listeners.keySet()) {
            mAnswersDbRef.child(id).removeEventListener(listeners.get(id));
        }
        handler = null;
    }

    public interface OnImpressionsReachedListener {
        void onImpressionsReached(String imgId);
    }
}
