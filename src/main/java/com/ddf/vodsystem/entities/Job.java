package com.ddf.vodsystem.entities;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

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
    private AtomicReference<Float> progress = new AtomicReference<>(0f);

    public Job(String uuid, File inputFile, File outputFile, VideoMetadata inputVideoMetadata) {
        this.uuid = uuid;
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.inputVideoMetadata = inputVideoMetadata;
    }
}
