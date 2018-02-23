package com.recoded.taqadam.models;

import android.net.Uri;

import com.recoded.taqadam.models.db.TaskDbHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by HP PC on 12/17/2017.
 */

public class Task {
    public static final String CATEGORIZATION = "categorization";
    public static final String BOUNDING_BOX = "bounding_box", BBOX = "bbox";

    private String taskId;
    private String jobId;
    private Uri taskImage;
    private Map<String, String> attemptedBy;
    private Map<String, String> completedBy;
    public Answer answer; //Only a reference;


    public Task(String id) {
        taskId = id;
        attemptedBy = new HashMap<>();
        completedBy = new HashMap<>();
    }

    public String getTaskId() {
        return taskId;
    }

    public String getJobId() {
        return jobId;
    }

    public Uri getTaskImage() {
        return taskImage;
    }

    public Map<String, String> getAttemptedBy() {
        return attemptedBy;
    }

    public Map<String, String> getCompletedBy() {
        return completedBy;
    }

    public void addAttempt(String uid, String answerId) {
        if (!attemptedBy.containsKey(uid)) {
            attemptedBy.put(uid, answerId);
        }
    }

    public void addComplete(String uid) {
        if (!completedBy.containsKey(uid) && attemptedBy.containsKey(uid)) {
            String answerId = attemptedBy.get(uid);
            attemptedBy.remove(uid);
            completedBy.put(uid, answerId);
        }
    }

    public Task fromMap(Map<String, Object> map) {
        for (String k : map.keySet()) {
            if (k.equals(TaskDbHandler.JOB_ID)) {
                jobId = (String) map.get(k);
            }
            if (k.equals(TaskDbHandler.TASK_IMAGE)) {
                taskImage = Uri.parse((String) map.get(k));
            }

            if (k.equals(TaskDbHandler.ATTEMPTS)) {
                attemptedBy.putAll((Map<String, String>) map.get(k));
            }

            if (k.equals(TaskDbHandler.COMPLETED_ATTEMPTS)) {
                completedBy.putAll((Map<String, String>) map.get(k));
            }
        }
        return this;
    }
}

