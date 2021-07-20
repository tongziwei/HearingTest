package com.example.hearingtest.adapter;

public class FrequencyBean {
    private int frequency;
    private int label;
    private boolean enable;

    public FrequencyBean(int frequency, int label, boolean enable) {
        this.frequency = frequency;
        this.label = label;
        this.enable = enable;
    }

    public FrequencyBean() {
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
