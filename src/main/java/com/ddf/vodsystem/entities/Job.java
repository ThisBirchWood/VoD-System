package com.ddf.vodsystem.entities;

import java.io.File;

import lombok.Data;

@Data
public class Job {
    private String uuid;
    private File inputFile;
    private File outputFile;

    // configs
    private VideoMetadata inputVideoMetadata;
    private VideoMetadata outputVideoMetadata = new VideoMetadata();

    // job status
    private JobStatus status = JobStatus.NOT_READY;
    private Float progress = 0.0f;

    public Job(String uuid, File inputFile, File outputFile, VideoMetadata inputVideoMetadata) {
        this.uuid = uuid;
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.inputVideoMetadata = inputVideoMetadata;
    }
}
