package com.ddf.vodsystem.services;

import com.ddf.vodsystem.dto.VideoMetadata;
import com.ddf.vodsystem.dto.Job;
import org.springframework.stereotype.Service;

@Service
public class EditService {
    private final JobService jobService;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EditService.class);

    public EditService(JobService jobService) {
        this.jobService = jobService;
    }

    public void edit(String uuid, VideoMetadata videoMetadata) {
        Job job = jobService.getJob(uuid);
        validateClipConfig(videoMetadata);
        job.setOutputVideoMetadata(videoMetadata);
    }

    public void process(String uuid) {
        Job job = jobService.getJob(uuid);
        jobService.processJob(job);
    }

    public void convert(String uuid) {
        Job job = jobService.getJob(uuid);
        jobService.convertJob(job);
    }

    public float getProgress(String uuid) {
        Job job = jobService.getJob(uuid);

        if (job.getStatus().getProcessTracker().isComplete()) {
            return 1f;
        }

        return job.getStatus().getProcessTracker().getProgress();
    }

    private void validateClipConfig(VideoMetadata videoMetadata) {
        Float start = videoMetadata.getStartPoint();
        Float end = videoMetadata.getEndPoint();
        Float fileSize = videoMetadata.getFileSize();
        Integer width = videoMetadata.getWidth();
        Integer height = videoMetadata.getHeight();
        Float fps = videoMetadata.getFps();

        if (start != null && start < 0) {
            throw new IllegalArgumentException("Start point cannot be negative");
        }

        if (end != null && end < 0) {
            throw new IllegalArgumentException("End point cannot be negative");
        }

        if (start != null && end != null && end <= start) {
            throw new IllegalArgumentException("End point must be greater than start point");
        }

        if (fileSize != null && fileSize < 100) {
            throw new IllegalArgumentException("File size cannot be less than 100kb");
        }

        if (width != null && width < 1) {
            throw new IllegalArgumentException("Width cannot be less than 1");
        }

        if (height != null && height < 1) {
            throw new IllegalArgumentException("Height cannot be less than 1");
        }

        if (fps != null && fps < 1) {
            throw new IllegalArgumentException("FPS cannot be less than 1");
        }
    }

}
