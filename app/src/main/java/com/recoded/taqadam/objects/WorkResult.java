package com.recoded.taqadam.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WorkResult {
    @Expose
    @SerializedName("project_name")
    private String projectName;
    @Expose
    @SerializedName("total_new_layer")
    private int totalNewLayer;
    @Expose
    @SerializedName("total_validated_layer")
    private int totalValidatedLayer;
    @Expose
    @SerializedName("total_new_object")
    private int totalnewObject;
    @Expose
    @SerializedName("total_validated_object")
    private int totalValidatedObject;
    @Expose
    @SerializedName("estimated_payment")
    private float estimatedPayment;
    @Expose
    @SerializedName("validated_payment")
    private float validatedPayment;

    public int getTotalNewLayer() {
        return totalNewLayer;
    }

    public void setTotalNewLayer(int totalNewLayer) {
        this.totalNewLayer = totalNewLayer;
    }

    public int getTotalValidatedLayer() {
        return totalValidatedLayer;
    }

    public void setTotalValidatedLayer(int totalValidatedLayer) {
        this.totalValidatedLayer = totalValidatedLayer;
    }

    public int getTotalnewObject() {
        return totalnewObject;
    }

    public void setTotalnewObject(int totalnewObject) {
        this.totalnewObject = totalnewObject;
    }

    public int getTotalValidatedObject() {
        return totalValidatedObject;
    }

    public void setTotalValidatedObject(int totalValidatedObject) {
        this.totalValidatedObject = totalValidatedObject;
    }

    public float getEstimatedPayment() {
        return estimatedPayment;
    }

    public void setEstimatedPayment(float estimatedPayment) {
        this.estimatedPayment = estimatedPayment;
    }

    public float getValidatedPayment() {
        return validatedPayment;
    }

    public void setValidatedPayment(float validatedPayment) {
        this.validatedPayment = validatedPayment;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
