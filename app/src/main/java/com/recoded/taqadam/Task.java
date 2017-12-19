package com.recoded.taqadam;

/**
 * Created by HP PC on 12/17/2017.
 */

public class Task {
    private String Type;
    private String title;
    private String description;

    public Task(String type, String title, String description) {
        Type = type;
        this.title = title;
        this.description = description;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}

