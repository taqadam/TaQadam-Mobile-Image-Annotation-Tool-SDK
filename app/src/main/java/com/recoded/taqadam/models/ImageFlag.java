package com.recoded.taqadam.models;

public class ImageFlag {
    private String label;
    private boolean selected = false;

    public ImageFlag(String label, boolean selected) {
        this.label = label;
        this.selected = selected;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
