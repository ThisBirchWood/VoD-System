package com.ddf.vodsystem.services.job;

import com.ddf.vodsystem.dto.ProgressTracker;
import com.ddf.vodsystem.dto.VideoMetadata;
import com.ddf.vodsystem.exceptions.FFMPEGException;
import com.ddf.vodsystem.services.media.RemuxService;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class RemuxStage implements JobStage {
    private JobStatus jobStatus = JobStatus.PENDING;
    private final ProgressTracker progress = new ProgressTracker(0.0f);
    private final RemuxService remuxService;

    public RemuxStage(RemuxService remuxService) {
        this.remuxService = remuxService;
    }

    @Override
    public void execute(Job job) {
        VideoMetadata videoMetadata = job.getInputVideoMetadata();

        try {
            jobStatus = JobStatus.RUNNING;
            remuxService.remux(job.getInputFile(), job.getOutputFile(), progress, videoMetadata.getEndPoint());
        } catch (IOException | InterruptedException e) {
            jobStatus = JobStatus.FAILED;
            progress.setProgress(0.0f);
            Thread.currentThread().interrupt();
            throw new FFMPEGException("Remuxing failed: " + e.getMessage());
        }
    }

    @Override
    public JobStatus getJobStatus() {
        return jobStatus;
    }

    @Override
    public float getProgress() {
        return progress.getProgress();
    }
}
