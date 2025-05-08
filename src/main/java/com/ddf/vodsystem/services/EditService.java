package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.EditDTO;
import com.ddf.vodsystem.entities.Job;
import org.springframework.stereotype.Service;

@Service
public class EditService {
    private final JobService jobService;

    public EditService(JobService jobService) {
        this.jobService = jobService;
    }

    public void edit(String uuid, EditDTO editDTO) {
        Job job = jobService.get(uuid);

        if (editDTO.getStartPoint() != null) {
            if (editDTO.getStartPoint() < 0) {
                throw new IllegalArgumentException("Start point cannot be negative");
            }

            job.setStartPoint(editDTO.getStartPoint());
        }

        if (editDTO.getEndPoint() != null) {
            job.setEndPoint(editDTO.getEndPoint());
        }

        if (editDTO.getFps() != null) {
            job.setFps(editDTO.getFps());
        }

        if (editDTO.getWidth() != null) {
            job.setWidth(editDTO.getWidth());
        }

        if (editDTO.getHeight() != null) {
            job.setHeight(editDTO.getHeight());
        }

        if (editDTO.getFileSize() != null) {
            if (editDTO.getFileSize() < 0) {
                throw new IllegalArgumentException("File size cannot be negative");
            }

            job.setFileSize(editDTO.getFileSize());
        }
    }

    public void jobReady(String uuid) {
        jobService.jobReady(uuid);
    }
}
