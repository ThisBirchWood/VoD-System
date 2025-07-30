package com.ddf.vodsystem.dto;

public class ProgressTracker {
    private float progress;

    public ProgressTracker(float initialProgress) {
        this.progress = initialProgress;
    }

    public synchronized void setProgress(float newProgress) {
        if (newProgress < 0 || newProgress > 1) {
            throw new IllegalArgumentException("Progress must be between 0 and 1");
        }
        this.progress = newProgress;
    }

    public synchronized float getProgress() {
        return progress;
    }
}
