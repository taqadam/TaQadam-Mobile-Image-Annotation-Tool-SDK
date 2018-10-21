package com.recoded.taqadam.models;

import com.google.gson.annotations.Expose;

public class Error {
    @Expose
    private String key;
    @Expose
    private String message;

    public Error(String key, String message) {
        this.key = key;
        this.message = message;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
