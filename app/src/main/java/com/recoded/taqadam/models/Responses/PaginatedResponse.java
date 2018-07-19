package com.recoded.taqadam.models.Responses;

import com.google.gson.annotations.Expose;

import java.util.List;

public class PaginatedResponse<T> {
    @Expose
    public List<T> data;
    @Expose
    public Links links;
    @Expose
    public Meta meta;
}
