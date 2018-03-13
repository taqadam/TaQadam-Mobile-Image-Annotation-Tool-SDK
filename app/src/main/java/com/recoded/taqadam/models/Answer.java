package com.recoded.taqadam.models;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hp on 1/12/2018.
 */

public class Answer {
    private Date answerStartTime;
    private String rawAnswerData;
    private Image image;
    private String jobId;

    public Answer(String jobId, Image image) {
        this.image = image;
        this.jobId = jobId;
        answerStartTime = new Date();
    }

    public void setRawAnswerData(String rawAnswerData) {
        this.rawAnswerData = rawAnswerData;
    }

    public String getRawAnswerData() {
        return rawAnswerData;
    }

    public Image getImage() {
        return image;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("start_time", answerStartTime.getTime());
        ret.put("submit_time", new Date().getTime());
        ret.put("job_id", jobId);

        //ret.put("answer_data", rawAnswerData.replace("\"", "\\\"")); //Escaped for FireBase
        ret.put("answer_data", rawAnswerData);
        return ret;
    }

    public String getJobId() {
        return jobId;
    }
}
