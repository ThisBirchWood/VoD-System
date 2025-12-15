package com.ddf.vodsystem.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.ddf.vodsystem.dto.Job;
import com.ddf.vodsystem.entities.User;
import com.ddf.vodsystem.services.media.CompressionService;
import com.ddf.vodsystem.services.media.RemuxService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ddf.vodsystem.exceptions.JobNotFound;

/**
 * Service for managing and processing jobs in a background thread.
 * Uses a blocking queue to avoid busy waiting and ensures jobs are processed sequentially.
 */
@Service
public class JobService {
    private static final Logger logger = LoggerFactory.getLogger(JobService.class);
    private final ConcurrentHashMap<String, Job> jobs = new ConcurrentHashMap<>();
    private final ClipService clipService;
    private final RemuxService remuxService;
    private final DirectoryService directoryService;
    private final CompressionService compressionService;
    private final UserService userService;

    /**
     * Constructs a JobService with the given CompressionService.
     * @param clipService the compression service to use for processing jobs
     */
    public JobService(ClipService clipService,
                      RemuxService remuxService,
                      CompressionService compressionService,
                      DirectoryService directoryService,
                      UserService userService) {
        this.clipService = clipService;
        this.remuxService = remuxService;
        this.directoryService = directoryService;
        this.compressionService = compressionService;
        this.userService = userService;
    }

    /**
     * Adds a new job to the job map.
     * @param job the job to add
     */
    public void add(Job job) {
        logger.info("Added job: {}", job.getUuid());
        jobs.put(job.getUuid(), job);
    }

    /**
     * Retrieves a job by its UUID.
     * @param uuid the UUID of the job
     * @return the Job object
     * @throws JobNotFound if the job does not exist
     */
    public Job getJob(String uuid) {
        Job job = jobs.get(uuid);

        if (job == null) {
            throw new JobNotFound("Job not found");
        }

        return job;
    }

    public void convertJob(Job job) {
        logger.info("Converting job: {}", job.getUuid());
        File tempFile = new File(job.getInputFile().getAbsolutePath() + ".temp");
        directoryService.copyFile(job.getInputFile(), tempFile);

        job.getStatus().getConversion().reset();

        try {
            remuxService.remux(
                    tempFile,
                    job.getInputFile(),
                    job.getStatus().getConversion(),
                    job.getInputClipOptions().getDuration())
                    .thenRun(() -> {
                        job.getStatus().getConversion().markComplete();

                        try {
                            Files.deleteIfExists(Paths.get(tempFile.getAbsolutePath()));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).whenComplete((ignored, throwable) -> {
                        if (throwable != null) {
                            logger.error("Remux failed for jobId={}", job.getUuid(), throwable);
                        } else {
                            logger.info("Remux completed for jobId={}", job.getUuid());
                        }
                    });

        } catch (IOException | InterruptedException e) {
            logger.error("Error converting job {}: {}", job.getUuid(), e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Marks a job as ready and adds it to the processing queue.
     * @param job The job to process
     */
    public void processJob(Job job) {
        logger.info("Job ready: {}", job.getUuid());
        job.getStatus().getProcess().reset();

        try {
            Optional<User> user = userService.getLoggedInUser();
            compressionService.compress(job.getInputFile(), job.getOutputFile(), job.getOutputClipOptions(), job.getStatus().getProcess())
                    .thenRun(() -> user.ifPresent(value ->
                            clipService.persistClip(
                                    job.getOutputClipOptions(),
                                    value,
                                    job.getOutputFile(),
                                    job.getInputFile().getName()
                            )
                    )).exceptionally(
                            ex -> {
                                job.getStatus().setFailed(true);
                                return null;
                            }
                    );
        } catch (IOException | InterruptedException e) {
            job.getStatus().setFailed(true);
            logger.error("Error processing job {}: {}", job.getUuid(), e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}