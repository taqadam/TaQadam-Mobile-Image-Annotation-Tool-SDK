
package com.recoded.taqadam.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.recoded.taqadam.models.Model;

import javax.annotation.Generated;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Assignment extends Model {
    private static final int ROLE_ANNOTATOR = 0;
    private static final int ROLE_QA = 1;

    @Expose
    private String name;
    @Expose
    private String desc;
    @Expose
    private Job job;
    @SerializedName("task_type")
    @Expose
    private Type type;
    @SerializedName("member_role")
    @Expose
    private int memberRole;

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

    public boolean forAnnotator() {
        return memberRole == ROLE_ANNOTATOR;
    }

    public boolean forValidator() {
        return memberRole == ROLE_QA;
    }
}
