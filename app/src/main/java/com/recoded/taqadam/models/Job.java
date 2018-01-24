package com.recoded.taqadam.models;

import com.recoded.taqadam.models.db.JobDbHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by hp on 1/10/2018.
 */

public class Job {
    private String jobId;
    private Date dateCreated;
    private Date dateExpires;
    private String type;
    private int numberOfAttempts;
    private int successfulAttempts;
    private String description;
    private float taskReward;
    private List<String> tasks;
    private String jobName;
    private String company;

    public Job(String jobId) {
        this.jobId = jobId;
        tasks = new ArrayList<>();
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

    public String getJobName() {
        return jobName;
    }

    public String getCompany() {
        return company;
    }

    public String getType() {
        return type;
    }

    public int getNumberOfAttempts() {
        return numberOfAttempts;
    }

    public int getSuccessfulAttempts() {
        return successfulAttempts;
    }

    public String getDescription() {
        return description;
    }

    public float getTaskReward() {
        return taskReward;
    }

    public List<String> getTasks() {
        return tasks;
    }

    public Job fromMap(Map<String, Object> map) {
        dateCreated = new Date((long) map.get(JobDbHandler.DATE_CREATED) * 1000);
        dateExpires = new Date((long) map.get(JobDbHandler.DATE_EXPIRES) * 1000);
        type = (String) map.get(JobDbHandler.TYPE);
        numberOfAttempts = ((Long) map.get(JobDbHandler.ATTEMPTS)).intValue();
        successfulAttempts = ((Long) map.get(JobDbHandler.SUCCESSFUL_ATTEMPTS)).intValue();
        jobName = (String) map.get(JobDbHandler.JOB_NAME);
        description = (String) map.get(JobDbHandler.DESC);
        company = (String) map.get(JobDbHandler.COMPANY);
        taskReward = ((long) map.get(JobDbHandler.TASK_REWARD)) / 100f;

        if (map.get(JobDbHandler.TASKS) != null)
            tasks.addAll((List<String>) map.get(JobDbHandler.TASKS));
        return this;
    }

    public Job update(Map<String, Object> value) {
        for (String k : value.keySet()) {
            switch (k) {
                case JobDbHandler.DATE_EXPIRES:
                    dateExpires.setTime((long) value.get(k) * 1000);
                    break;
                case JobDbHandler.TYPE:
                    type = (String) value.get(k);
                    break;
                case JobDbHandler.JOB_NAME:
                    jobName = (String) value.get(k);
                    break;
                case JobDbHandler.DESC:
                    description = (String) value.get(k);
                    break;
                case JobDbHandler.TASK_REWARD:
                    taskReward = ((long) value.get(k)) / 100f;
                    break;
                case JobDbHandler.TASKS:
                    if (value.get(k) != null)
                        tasks.clear();
                    tasks.addAll((List<String>) value.get(k));
                    break;
            }
        }
        return this;
    }
}
