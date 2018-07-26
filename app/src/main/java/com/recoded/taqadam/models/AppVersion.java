package com.recoded.taqadam.models;

import com.google.gson.annotations.Expose;

/**
 * Created by wisam on Feb 23 18.
 */

public class AppVersion {
    @Expose
    public Long code;
    @Expose
    public String version;
    @Expose
    public Boolean required;
}
