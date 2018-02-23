package com.recoded.taqadam.models;

import com.recoded.taqadam.models.db.JobDbHandler;
import com.recoded.taqadam.models.db.TaskDbHandler;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private int noOfImpressions;
    private String tasksType;
    private List<String> options;
    private String instructions;
    private String description;
    private float taskReward;
    private List<String> tasks;
    private String jobName;
    private String company;

    public Job(String jobId) {
        this.jobId = jobId;
        tasks = new ArrayList<>();
        options = new ArrayList<>();
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

    public List<String> getOptions() {
        return options;
    }

    public String getJobName() {
        return jobName;
    }

    public String getCompany() {
        return company;
    }

    public int getNoOfImpressions() {
        return noOfImpressions;
    }

    public String getTasksType() {
        return tasksType;
    }

    public String getInstructions() {
        return instructions;
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
        for (String k : map.keySet()) {
            switch (k) {
                case JobDbHandler.DATE_CREATED:
                    if (dateCreated == null) {
                        dateCreated = new Date((long) map.get(k) * 1000);
                    } else {
                        dateCreated.setTime((long) map.get(k) * 1000);
                    }
                    break;
                case JobDbHandler.DATE_EXPIRES:
                    if (dateExpires == null) {
                        dateExpires = new Date((long) map.get(k) * 1000);
                    } else {
                        dateExpires.setTime((long) map.get(k) * 1000);
                    }
                    break;
                case JobDbHandler.TYPE:
                    type = (String) map.get(k);
                    break;
                case JobDbHandler.TASKS_TYPE:
                    tasksType = (String) map.get(k);
                    break;
                case JobDbHandler.IMPRESSIONS:
                    noOfImpressions = ((Long) map.get(k)).intValue();
                    break;
                case JobDbHandler.INSTRUCTIONS:
                    instructions = (String) map.get(k);
                    break;
                case JobDbHandler.JOB_NAME:
                    jobName = (String) map.get(k);
                    break;
                case JobDbHandler.DESC:
                    description = (String) map.get(k);
                    break;
                case JobDbHandler.COMPANY:
                    company = (String) map.get(k);
                    break;
                case JobDbHandler.ATTEMPTS:
                    numberOfAttempts = ((Long) map.get(k)).intValue();
                    break;
                case JobDbHandler.SUCCESSFUL_ATTEMPTS:
                    successfulAttempts = ((Long) map.get(k)).intValue();
                    break;

                case JobDbHandler.OPTIONS:
                    options.addAll((List<String>) map.get(k));
                    break;

                case JobDbHandler.TASK_REWARD:
                    if (map.get(k) instanceof Double) {
                        double tReward = (double) map.get(k);
                        BigDecimal bd = new BigDecimal(tReward);
                        bd = bd.setScale(2, RoundingMode.DOWN);
                        taskReward = bd.floatValue();
                    } else {
                        taskReward = ((long) map.get(k)) * 1f;
                    }
                    break;
                case JobDbHandler.TASKS:
                    if (!tasks.isEmpty()) {
                        for (String taskId : tasks) {
                            TaskDbHandler.getInstance().removeTask(taskId);
                        }
                        tasks.clear();
                    }
                    tasks.addAll((List<String>) map.get(k));
                    break;
            }
        }

        return this;
    }
}
