
package com.recoded.taqadam.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Assignment extends Model {
    @Expose
    private String name;
    @Expose
    private String desc;
    @Expose
    private Job job;
    @SerializedName("task_type")
    @Expose
    private Type type;

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
}
