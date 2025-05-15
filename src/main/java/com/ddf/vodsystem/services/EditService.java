package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.ClipConfig;
import com.ddf.vodsystem.entities.Job;
import com.ddf.vodsystem.entities.JobStatus;
import com.ddf.vodsystem.exceptions.JobNotFound;
import org.springframework.stereotype.Service;

@Service
public class EditService {
    private final JobService jobService;

    public EditService(JobService jobService) {
        this.jobService = jobService;
    }

    public void edit(String uuid, ClipConfig clipConfig) {
        Job job = jobService.get(uuid);

        if (clipConfig.getStartPoint() != null && clipConfig.getStartPoint() < 0) {
            throw new IllegalArgumentException("Start point cannot be negative");
        }

        if (clipConfig.getFileSize() != null && clipConfig.getFileSize() < 100) {
            throw new IllegalArgumentException("File size cannot be less than 100kb");
        }

        job.setClipConfig(clipConfig);
    }

    public void process(String uuid) {
        jobService.jobReady(uuid);
    }

    public float getProgress(String uuid) {
        Job job = jobService.get(uuid);

        if (job == null) {
            throw new JobNotFound(uuid);
        }

        if (job.getStatus() == JobStatus.FINISHED) {
            return 1f;
        }

        return job.getProgress();
    }
}
