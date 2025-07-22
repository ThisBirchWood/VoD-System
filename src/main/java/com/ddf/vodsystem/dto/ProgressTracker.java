package com.ddf.vodsystem.dto;

import lombok.Getter;

@Getter
public class ProgressTracker {
    private float progress;

    public ProgressTracker(float initialProgress) {
        this.progress = initialProgress;
    }

    public void setProgress(float newProgress) {
        if (newProgress < 0 || newProgress > 1) {
            throw new IllegalArgumentException("Progress must be between 0 and 1");
        }
        this.progress = newProgress;
    }
}
