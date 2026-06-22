package com.ddf.vodsystem.dto;

import lombok.Data;

import java.io.File;
import java.time.Instant;

@Data
public class Job {
    private String uuid;
    private ProgressTracker progressTracker = new ProgressTracker();
    private JobState state = JobState.READY;
    private String errorOutput;

    private File download;
    private Instant createdAt = Instant.now();

    public Job(String uuid) {
        this.uuid = uuid;
    }
}
