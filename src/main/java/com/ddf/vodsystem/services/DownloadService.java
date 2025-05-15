package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.JobStatus;
import com.ddf.vodsystem.exceptions.JobNotFinished;
import com.ddf.vodsystem.exceptions.JobNotFound;
import com.ddf.vodsystem.entities.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class DownloadService {

    private final JobService jobService;

    @Autowired
    public DownloadService(JobService jobService) {
        this.jobService = jobService;
    }

    public Resource downloadInput(String uuid) {
        Job job = jobService.get(uuid);

        if (job == null) {
            throw new JobNotFound("Job doesn't exist");
        }

        File file = job.getInputFile();
        return new FileSystemResource(file);
    }

    public Resource downloadOutput(String uuid) {
        Job job = jobService.get(uuid);

        if (job == null) {
            throw new JobNotFound("Job doesn't exist");
        }

        if (job.getStatus() != JobStatus.FINISHED) {
            throw new JobNotFinished("Job is not finished");
        }

        File file = job.getOutputFile();
        return new FileSystemResource(file);
    }
}
