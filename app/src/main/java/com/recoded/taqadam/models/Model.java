package com.recoded.taqadam.models;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class Model implements Serializable {

    @Expose
    protected Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
