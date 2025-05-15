package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.ClipConfig;
import com.ddf.vodsystem.entities.Job;
import com.ddf.vodsystem.entities.JobStatus;
import org.springframework.stereotype.Service;

@Service
public class EditService {
    private final JobService jobService;

    public EditService(JobService jobService) {
        this.jobService = jobService;
    }

    public void edit(String uuid, ClipConfig clipConfig) {
        Job job = jobService.getJob(uuid);
        validateClipConfig(clipConfig);
        job.setClipConfig(clipConfig);
    }

    public void process(String uuid) {
        jobService.jobReady(uuid);
    }

    public float getProgress(String uuid) {
        Job job = jobService.getJob(uuid);

        if (job.getStatus() == JobStatus.FINISHED) {
            return 1f;
        }

        return job.getProgress();
    }

    private void validateClipConfig(ClipConfig clipConfig) {
        Float start = clipConfig.getStartPoint();
        Float end = clipConfig.getEndPoint();
        Float fileSize = clipConfig.getFileSize();

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
    }
}
