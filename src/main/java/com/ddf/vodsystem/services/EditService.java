package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.ClipConfig;
import com.ddf.vodsystem.tools.Job;
import org.springframework.stereotype.Service;

@Service
public class EditService {
    private final JobService jobService;

    public EditService(JobService jobService) {
        this.jobService = jobService;
    }

    public void edit(String uuid, ClipConfig clipConfig) {
        Job job = jobService.get(uuid);

        if (clipConfig.getStartPoint() != null) {
            if (clipConfig.getStartPoint() < 0) {
                throw new IllegalArgumentException("Start point cannot be negative");
            }
        }

        job.setClipConfig(clipConfig);


    }

    public void jobReady(String uuid) {
        jobService.jobReady(uuid);
    }
}
