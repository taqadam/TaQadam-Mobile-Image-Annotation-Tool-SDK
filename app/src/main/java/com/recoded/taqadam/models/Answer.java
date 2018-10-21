package com.recoded.taqadam.models;

import com.google.gson.annotations.Expose;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by hp on 1/12/2018.
 */
@Entity(tableName = "answers")
public class Answer extends Model {


    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private Integer dbId; //For local database only

    @ColumnInfo(name = "time_taken")
    @Expose
    private Long timeTaken;

    @ColumnInfo(name = "started_at")
    @Expose
    private Long startedAt;

    @ColumnInfo(name = "data")
    @Expose
    private String data;

    @ColumnInfo(name = "task_id")
    @Expose
    private Long taskId;

    @ColumnInfo(name = "assignment_id")
    @Expose
    private Long assignmentId;

    @ColumnInfo(name = "submitted_at")
    @Expose
    private Long submittedAt;

    public Answer(Long assignmentId, Long taskId) {
        this.taskId = taskId;
        this.assignmentId = assignmentId;
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

    public void setTimeTaken(Long timeTaken) {
        this.timeTaken = timeTaken;
    }

    public Long getTimeTaken() {
        return timeTaken;
    }

    public void setStartedAt(Long time) {
        startedAt = time;
    }

    public Long getStartedAt() {
        return startedAt;
    }

    public void setSubmittedAt(Long time) {
        submittedAt = time;
    }

    public Long getSubmittedAt() {
        return submittedAt;
    }

    public Integer getDbId() {
        return dbId;
    }

    public void setDbId(Integer uid) {
        this.dbId = uid;
    }
}
