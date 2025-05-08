package com.ddf.vodsystem.entities;

import lombok.Data;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public class Job implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Job.class);

    private String uuid;
    private File file;

    // configs
    private float startPoint;
    private float endPoint;
    private float fps;
    private int width;
    private int height;
    private float fileSize;

    // job status
    private JobStatus status = JobStatus.PENDING;

    public Job(String uuid, File file) {
        this.uuid = uuid;
        this.file = file;
    }

    @Override
    public void run() {
        logger.info("Job started");
        this.status = JobStatus.RUNNING;

        this.status = JobStatus.FINISHED;

    }
}
