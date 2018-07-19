package com.recoded.taqadam.models;

import com.google.gson.annotations.Expose;

import java.util.Date;

/**
 * Created by hp on 1/12/2018.
 */

public class Answer extends Model {

    @Expose
    private Long startedAt;
    @Expose
    private String data;
    @Expose
    private Long taskId;
    @Expose
    private Long assignmentId;

    @Expose
    private Long submittedAt; //just a place holder;

    public Answer(Long assignmentId, Long taskId) {
        this.taskId = taskId;
        this.assignmentId = assignmentId;
        startedAt = new Date().getTime();
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getStartedAt() {
        return startedAt;
    }

    public void setSubmittedAt() {
        submittedAt = new Date().getTime();
    }

    public Long getSubmittedAt() {
        return submittedAt;
    }
}
