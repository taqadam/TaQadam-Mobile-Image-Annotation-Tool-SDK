package com.recoded.taqadam.models.db;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.recoded.taqadam.models.Job;
import com.recoded.taqadam.models.auth.UserAuthHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Ahmad Siafaddin on 1/10/2018.
 */

public class JobDbHandler {

    public static final String
            JOB_NAME = "job_name",
            DESC = "description",
            COMPANY = "company",
            DATE_CREATED = "date_created",
            DATE_EXPIRES = "date_expires",
            TYPE = "type",
            ATTEMPTS = "number_of_attempts",
            SUCCESSFUL_ATTEMPTS = "successful_attempts",
            TASK_REWARD = "task_reward",
            TASKS = "tasks";

    private static JobDbHandler handler;
    private HashMap<String, Job> jobList;

    private String mUid;
    private DatabaseReference mJobsDbRef;
    private ChildEventListener mJobsListener;
    private OnJobsChangedListener listener;

    private JobDbHandler() {
        mUid = UserAuthHandler.getInstance().getUid();
        this.mJobsDbRef = FirebaseDatabase.getInstance().getReference()
                .child("Temp").child("Jobs");
        this.mJobsDbRef.keepSynced(true);
        this.jobList = new HashMap<>();

        setupLatestJobsListener();
    }

    public synchronized static JobDbHandler getInstance() {
        if (handler == null) {
            handler = new JobDbHandler();
        }
        return handler;
    }

    private void setupLatestJobsListener() {
        this.mJobsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Job job = new Job(dataSnapshot.getKey())
                        .fromMap((Map<String, Object>) dataSnapshot.getValue());
                if (job.getDateExpires().getTime() > System.currentTimeMillis())
                    jobList.put(job.getJobId(), job);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Job j = jobList.get(dataSnapshot.getKey()).update((Map<String, Object>) dataSnapshot.getValue());
                if (j.getDateExpires().getTime() < System.currentTimeMillis()) {
                    jobList.remove(dataSnapshot.getKey());
                } else {
                    jobList.put(dataSnapshot.getKey(), j);
                }
                if (listener != null) listener.onJobsChanged(getRecentJobs());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                jobList.remove(dataSnapshot.getKey());
                if (listener != null) listener.onJobsChanged(getRecentJobs());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mJobsDbRef.addChildEventListener(mJobsListener);
        mJobsDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == jobList.size()) {
                    if (listener != null) listener.onJobsChanged(getRecentJobs());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public List<Job> getRecentJobs() {
        List<Job> res = new ArrayList<>();
        for (String key : jobList.keySet()) {
            res.add(jobList.get(key));
        }
        return res;
    }

    public void setOnJobsChangedListener(OnJobsChangedListener listener) {
        this.listener = listener;
    }

    public OnJobsChangedListener getOnJobsChangedLister() {
        return listener;
    }

    public Job getJob(String id) {
        if (jobList.containsKey(id)) {
            return jobList.get(id);
        }

        return new Job("null");
    }

    public void attemptJob(final Job job) {
        DatabaseReference attemptedRef = mJobsDbRef.child(job.getJobId()).child(ATTEMPTS);
        attemptedRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                int attempts = (int) mutableData.getValue();
                mutableData.setValue(++attempts);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });
    }

    public void release() {
        listener = null;
        this.jobList.clear();
        this.mUid = null;
        mJobsDbRef.removeEventListener(mJobsListener);
        mJobsListener = null;
        handler = null;
    }

    public interface OnJobsChangedListener {
        void onJobsChanged(List<Job> jobs);
    }
}
