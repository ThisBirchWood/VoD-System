package com.ddf.vodsystem.tools;

import com.ddf.vodsystem.entities.ClipConfig;
import com.ddf.vodsystem.entities.JobStatus;
import com.ddf.vodsystem.services.CompressionService;
import lombok.Data;
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public class Job implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Job.class);

    private String uuid;
    private File inputFile;
    private File outputFile;

    // configs
    private ClipConfig clipConfig;

    // job status
    private JobStatus status = JobStatus.PENDING;
    private Float progress = 0.0f;

    public Job(String uuid, File inputFile, File outputFile) {
        this.uuid = uuid;
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    @Override
    public void run() {
        logger.info("Job {} started", uuid);
        this.status = JobStatus.RUNNING;

        CompressionService f = new CompressionService(inputFile, outputFile, clipConfig);

        try {
            f.run();
        } catch (IOException | InterruptedException e) {
            logger.error(e.getMessage());
        }


        this.status = JobStatus.FINISHED;
        inputFile.delete();
        logger.info("Job {} finished", uuid);
    }
}
