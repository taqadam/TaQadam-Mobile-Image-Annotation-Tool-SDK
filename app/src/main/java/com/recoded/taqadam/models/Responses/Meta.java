package com.recoded.taqadam.models.Responses;

import com.google.gson.annotations.Expose;

public class Meta {
    @Expose
    public Long currentPage; //response current_page
    @Expose
    public Long from; //set from
    @Expose
    public Long lastPage; //response last_page
    @Expose
    public String path; //resource path
    @Expose
    public Long perPage; //response per_page
    @Expose
    public Long to; //set to
    @Expose
    public Long total; //set total
}
