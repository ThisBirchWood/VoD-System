package com.ddf.vodsystem.services.job;

public interface JobStage {
    void execute(Job job);
    JobStatus getJobStatus();
    float getProgress();
}
