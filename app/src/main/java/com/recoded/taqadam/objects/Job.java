
package com.recoded.taqadam.objects;

import com.google.gson.annotations.Expose;
import com.recoded.taqadam.models.Dataset;
import com.recoded.taqadam.models.Model;
import com.recoded.taqadam.models.Service;

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
