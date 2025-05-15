package com.ddf.vodsystem.entities;

import lombok.Data;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public class Job {
    private static final Logger logger = LoggerFactory.getLogger(Job.class);

    private String uuid;
    private File inputFile;
    private File outputFile;

    // configs
    private ClipConfig clipConfig;

    // job status
    private JobStatus status = JobStatus.NOT_READY;
    private Float progress = 0.0f;

    public Job(String uuid, File inputFile, File outputFile) {
        this.uuid = uuid;
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }
}
