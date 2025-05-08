package com.ddf.vodsystem.entities;

import com.ddf.vodsystem.services.FfmpegService;
import lombok.Data;
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public class Job implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Job.class);

    private String uuid;
    private File file;

    // configs
    private Float startPoint;
    private Float endPoint;
    private Float fps;
    private Integer width;
    private Integer height;
    private Float fileSize;

    // job status
    private JobStatus status = JobStatus.PENDING;
    private Float progress = 0.0f;

    public Job(String uuid, File file) {
        this.uuid = uuid;
        this.file = file;
    }

    @Override
    public void run() {
        logger.info("Job {} started", uuid);
        this.status = JobStatus.RUNNING;

        FfmpegService f = new FfmpegService(file, new File("output.mp4"));
        f.setStartPoint(startPoint);
        f.setEndPoint(endPoint);
        f.setFps(fps);
        f.setWidth(width);
        f.setHeight(height);
        f.setFileSize(fileSize);

        try {
            f.run();
        } catch (IOException | InterruptedException e) {
            logger.error(e.getMessage());
        }


        this.status = JobStatus.FINISHED;
        file.delete();
        logger.info("Job {} finished", uuid);
    }
}
