package com.ddf.vodsystem.dto;

public class ProgressTracker {
    private float progress = 0.0f;
    private boolean isComplete = false;

    public synchronized float getProgress() {
        return progress;
    }

    public synchronized boolean isComplete() {
        return isComplete;
    }

    public synchronized void setProgress(float newProgress) {
        if (newProgress < 0 || newProgress > 1) {
            throw new IllegalArgumentException("Progress must be between 0 and 1");
        }
        this.progress = newProgress;
    }

    public synchronized void markComplete() {
        this.isComplete = true;
    }
}
