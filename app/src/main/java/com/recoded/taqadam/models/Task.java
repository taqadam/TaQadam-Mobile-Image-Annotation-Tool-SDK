package com.recoded.taqadam.models;

import android.net.Uri;

import com.recoded.taqadam.models.db.TaskDbHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by HP PC on 12/17/2017.
 */

public class Task {
    public static final String CATEGORIZATION = "categorization";
    public static final String BOUNDING_BOX = "bounding_box", BBOX = "bbox";

    private String taskId;
    private String jobId;
    private Date dateCreated;
    private Date dateExpires;
    private Uri taskImage;
    private String type;
    private String description;
    private List<String> options;
    private Map<String, String> attemptedBy;
    private Map<String, String> completedBy;
    public Answer answer; //Only a reference;


    public Task(String id) {
        taskId = id;
        options = new ArrayList<>();
        attemptedBy = new HashMap<>();
        completedBy = new HashMap<>();
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getJobId() {
        return jobId;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public Date getDateExpires() {
        return dateExpires;
    }

    public Uri getTaskImage() {
        return taskImage;
    }

    public List<String> getOptions() {
        return options;
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
        type = (String) map.get(TaskDbHandler.TYPE);
        jobId = (String) map.get(TaskDbHandler.JOB_ID);
        dateCreated = new Date((long) map.get(TaskDbHandler.DATE_CREATED) * 1000);
        dateExpires = new Date((long) map.get(TaskDbHandler.DATE_EXPIRES) * 1000);
        description = (String) map.get(TaskDbHandler.DESC);
        taskImage = Uri.parse((String) map.get(TaskDbHandler.TASK_IMAGE));

        if (map.get(TaskDbHandler.ATTEMPTS) != null)
            attemptedBy.putAll((Map<String, String>) map.get(TaskDbHandler.ATTEMPTS));

        if (map.get(TaskDbHandler.COMPLETED_ATTEMPTS) != null)
            completedBy.putAll((Map<String, String>) map.get(TaskDbHandler.COMPLETED_ATTEMPTS));

        if (map.get(TaskDbHandler.OPTIONS) != null)
            options.addAll((List<String>) map.get(TaskDbHandler.OPTIONS));
        return this;
    }
}

