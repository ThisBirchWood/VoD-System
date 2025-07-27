package com.ddf.vodsystem.entities;

import java.io.File;

import com.ddf.vodsystem.dto.ProgressTracker;
import com.ddf.vodsystem.dto.VideoMetadata;
import org.springframework.security.core.context.SecurityContext;

import lombok.Data;

@Data
public class Job {
    private String uuid;
    private File inputFile;
    private File outputFile;

    // configs
    private VideoMetadata inputVideoMetadata;
    private VideoMetadata outputVideoMetadata = new VideoMetadata();

    // security
    private SecurityContext securityContext;

    // job status
    private JobStatus status = JobStatus.NOT_READY;
    private ProgressTracker progress = new ProgressTracker(0.0f);
    private ProgressTracker mp4ConversionProgress = new ProgressTracker(0.0f);

    public Job(String uuid, File inputFile, File outputFile, VideoMetadata inputVideoMetadata) {
        this.uuid = uuid;
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.inputVideoMetadata = inputVideoMetadata;
    }
}
