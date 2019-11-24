package com.recoded.taqadam.models;

import java.util.Map;

public class Label {
    private String label;
    private Map<String, Label> children;

    public String getLabel() {
        return label;
    }

    public boolean hasChildren(){
        return children != null && children.size() > 0;
    }

    public Map<String, Label> getChildren() {
        return children;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setChildren(Map<String, Label> children) {
        this.children = children;
    }
}
