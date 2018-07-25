
package com.recoded.taqadam.models;

import com.google.gson.annotations.Expose;

import java.util.List;

import javax.annotation.Generated;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Job extends Model {

    @Expose
    private Dataset dataset;
    @Expose
    private String description;
    @Expose
    private String instructions;
    @Expose
    private List<String> options;
    @Expose
    private User owner;
    @Expose
    private String requiredBefore;
    @Expose
    private Service service;
    @Expose
    private String title;

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getRequiredBefore() {
        return requiredBefore;
    }

    public void setRequiredBefore(String requiredBefore) {
        this.requiredBefore = requiredBefore;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
