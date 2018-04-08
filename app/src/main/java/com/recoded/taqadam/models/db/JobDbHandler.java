package com.recoded.taqadam.models.db;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
            MULTI_CHOICE = "multi_choice",
            TASKS_TYPE = "tasks_type",
            OPTIONS = "options",
            COUNT = "images_count",
            INSTRUCTIONS = "instructions",
            IMPRESSIONS = "impressions",
            TASK_REWARD = "task_reward";

    private static JobDbHandler handler;
    private HashMap<String, Job> jobsCache;

    private String mUid;
    private DatabaseReference mJobsDbRef;
    private ChildEventListener mJobsListener;
    private OnJobsChangedListener listener;

    private JobDbHandler() {
        mUid = UserAuthHandler.getInstance().getUid();
        this.mJobsDbRef = FirebaseDatabase.getInstance().getReference()
                .child("Work").child("Jobs");
        this.mJobsDbRef.keepSynced(true);
        this.jobsCache = new HashMap<>();

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
                if (job.getDateExpires().getTime() > System.currentTimeMillis()) {
                    if (job.getType().equalsIgnoreCase("Qualifier")
                            || job.getType().equalsIgnoreCase("Tutorial")
                            || (job.getType().equalsIgnoreCase("Paid")
                            && UserAuthHandler.getInstance().getCurrentUser().isAccountApproved())) {
                        jobsCache.put(job.getJobId(), job);
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Job job = jobsCache.get(dataSnapshot.getKey()).fromMap((Map<String, Object>) dataSnapshot.getValue());
                if (job.getDateExpires().getTime() < System.currentTimeMillis()
                        || (job.getType().equalsIgnoreCase("Paid")
                        && !UserAuthHandler.getInstance().getCurrentUser().isAccountApproved())) {
                    jobsCache.remove(dataSnapshot.getKey());
                } else {
                    jobsCache.put(dataSnapshot.getKey(), job);
                }
                if (listener != null) listener.onJobsChanged(getRecentJobs());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                jobsCache.remove(dataSnapshot.getKey());
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
                if (listener != null) listener.onJobsChanged(getRecentJobs());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public List<Job> getRecentJobs() {
        List<Job> res = new ArrayList<>();
        for (String key : jobsCache.keySet()) {
            res.add(jobsCache.get(key));
        }
        return res;
    }

    public void setOnJobsChangedListener(OnJobsChangedListener listener) {
        this.listener = listener;
    }

    public Job getJob(String id) {
        return jobsCache.get(id);
    }

    public void release() {
        listener = null;
        this.jobsCache.clear();
        this.mUid = null;
        mJobsDbRef.removeEventListener(mJobsListener);
        mJobsListener = null;
        handler = null;
    }

    public interface OnJobsChangedListener {
        void onJobsChanged(List<Job> jobs);
    }
}
