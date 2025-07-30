package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.Clip;
import com.ddf.vodsystem.services.job.JobStatus;
import com.ddf.vodsystem.exceptions.JobNotFinished;
import com.ddf.vodsystem.exceptions.JobNotFound;
import com.ddf.vodsystem.dto.Job;
import com.ddf.vodsystem.exceptions.NotAuthenticated;
import com.ddf.vodsystem.repositories.ClipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class DownloadService {

    private final JobService jobService;
    private final ClipRepository clipRepository;
    private final ClipService clipService;

    @Autowired
    public DownloadService(JobService jobService,
                           ClipRepository clipRepository,
                           ClipService clipService) {
        this.jobService = jobService;
        this.clipRepository = clipRepository;
        this.clipService = clipService;
    }

    public Resource downloadInput(String uuid) {
        Job job = jobService.getJob(uuid);

        if (job == null) {
            throw new JobNotFound("Job doesn't exist");
        }

        File file = job.getInputFile();
        return new FileSystemResource(file);
    }

    public Resource downloadOutput(String uuid) {
        Job job = jobService.getJob(uuid);

        if (job == null) {
            throw new JobNotFound("Job doesn't exist");
        }

        if (job.getStatus() != JobStatus.FINISHED) {
            throw new JobNotFinished("Job is not finished");
        }

        File file = job.getOutputFile();
        return new FileSystemResource(file);
    }

    public Resource downloadClip(Clip clip) {
        if (!clipService.isAuthenticatedForClip(clip)) {
            throw new NotAuthenticated("Not authenticated for this clip");
        }

        String path = clip.getVideoPath();
        File file = new File(path);

        if (!file.exists()) {
            throw new JobNotFound("Clip file not found");
        }

        return new FileSystemResource(file);
    }

    public Resource downloadClip(Long id) {
        Clip clip = clipRepository.findById(id).orElseThrow(() -> new JobNotFound("Clip not found with id: " + id));
        return downloadClip(clip);
    }

    public Resource downloadThumbnail(Clip clip) {
        if (!clipService.isAuthenticatedForClip(clip)) {
            throw new NotAuthenticated("Not authenticated for this clip thumbnail");
        }

        String path = clip.getThumbnailPath();
        File file = new File(path);
        if (!file.exists()) {
            throw new JobNotFound("Thumbnail file not found");
        }

        return new FileSystemResource(file);
    }

    public Resource downloadThumbnail(Long id) {
        Clip clip = clipRepository.findById(id).orElseThrow(() -> new JobNotFound("Clip not found with id: " + id));
        return downloadThumbnail(clip);
    }
}
