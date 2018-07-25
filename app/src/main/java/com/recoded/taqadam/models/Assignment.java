
package com.recoded.taqadam.models;

import com.google.gson.annotations.Expose;

import java.util.Date;

import javax.annotation.Generated;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Assignment extends Model {

    @Expose
    private String details;
    @Expose
    private String difficulty;
    @Expose
    private Date expires;
    @Expose
    private Job job;
    @Expose
    private Type type;
    //@Expose
    //private List<Task> tasks;

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Double getDifficulty() {
        return Double.parseDouble(difficulty);
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    /*
    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks){
        this.tasks = tasks;
    }
    */
}
