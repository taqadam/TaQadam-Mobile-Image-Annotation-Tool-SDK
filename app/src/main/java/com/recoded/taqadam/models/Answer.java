package com.recoded.taqadam.models;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by hp on 1/12/2018.
 */

public class Answer {
    private String taskId;
    private String answerId;
    private String userId;
    private boolean isCompleted;
    private Date answerStartTime;
    private Date answerSubmitTime;
    private String rawAnswerData;
    private String answerAccurancy;

    public String getTaskId() {
        return taskId;
    }

    public String getAnswerId() {
        return answerId;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public Date getAnswerStartTime() {
        return answerStartTime;
    }

    public Date getAnswerSubmitTime() {
        return answerSubmitTime;
    }

    public String getRawAnswerData() {
        return rawAnswerData;
    }

    public String getAnswerAccurancy() {
        return answerAccurancy;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void setAnswerId(String answerId) {
        this.answerId = answerId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public void setAnswerStartTime(Date answerStartTime) {
        this.answerStartTime = answerStartTime;
    }

    public void setAnswerSubmitTime(Date answerSubmitTime) {
        this.answerSubmitTime = answerSubmitTime;
    }

    public void setRawAnswerData(String rawAnswerData) {
        this.rawAnswerData = rawAnswerData;
    }

    public void setAnswerAccurancy(String answerAccurancy) {
        this.answerAccurancy = answerAccurancy;
    }

    public static Answer fromMap(HashMap map) {
        String id = "";
        Answer answer = new Answer();
        answer.answerId = (String) map.get("");
        answer.taskId = (String) map.get("");
        answer.userId = (String) map.get("");
        answer.answerStartTime = (Date) map.get("");
        answer.answerSubmitTime = (Date) map.get("");
        answer.answerAccurancy = (String) map.get("");
        answer.isCompleted = (boolean) map.get("");
        answer.rawAnswerData = (String) map.get("");

        return answer;
    }
}
