package com.recoded.taqadam.models;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    public Answer(String taskId) {
        this(taskId, null);
    }

    public Answer(String taskId, String answerId) {
        this.taskId = taskId;
        this.answerId = answerId;
        isCompleted = false;
    }

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
        answerSubmitTime = new Date();
    }

    public void setAnswerStartTime(Date answerStartTime) {
        this.answerStartTime = answerStartTime;
    }

    /*public void setAnswerSubmitTime(Date answerSubmitTime) {
        this.answerSubmitTime = answerSubmitTime;
    }*/

    public void setRawAnswerData(String rawAnswerData) {
        this.rawAnswerData = rawAnswerData;
    }

    public Answer fromMap(Map<String, Object> map) {
        userId = (String) map.get("uid");
        answerStartTime = new Date((long) map.get("start_time"));

        if (map.containsKey("submit_time"))
            answerSubmitTime = new Date((long) map.get("submit_time"));

        isCompleted = (boolean) map.get("is_completed");

        if (map.containsKey("answer_data")) {
            String rawAnswerDataTemp = (String) map.get("answer_data");
            rawAnswerData = rawAnswerDataTemp.replace("\\\"", "\"");
        }

        return this;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("uid", userId);
        ret.put("start_time", answerStartTime.getTime());

        if (answerSubmitTime != null)
            ret.put("submit_time", answerSubmitTime.getTime());

        if (rawAnswerData != null && !rawAnswerData.isEmpty())
            ret.put("answer_data", rawAnswerData.replace("\"", "\\\"")); //Escaped for FireBase

        ret.put("is_completed", isCompleted);
        return ret;
    }
}
