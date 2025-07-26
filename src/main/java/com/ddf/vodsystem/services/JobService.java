package com.ddf.vodsystem.services;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ddf.vodsystem.entities.Job;
import com.ddf.vodsystem.entities.JobStatus;
import com.ddf.vodsystem.exceptions.JobNotFound;

import jakarta.annotation.PostConstruct;

/**
 * Service for managing and processing jobs in a background thread.
 * Uses a blocking queue to avoid busy waiting and ensures jobs are processed sequentially.
 */
@Service
public class JobService {
    private static final Logger logger = LoggerFactory.getLogger(JobService.class);
    private final ConcurrentHashMap<String, Job> jobs = new ConcurrentHashMap<>();
    private final BlockingQueue<Job> jobQueue = new LinkedBlockingQueue<>();
    private final ClipService clipService;

    /**
     * Constructs a JobService with the given CompressionService.
     * @param clipService the compression service to use for processing jobs
     */
    public JobService(ClipService clipService) {
        this.clipService = clipService;
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

    /**
     * Marks a job as ready and adds it to the processing queue.
     * @param uuid the UUID of the job to mark as ready
     */
    public void jobReady(String uuid) {
        Job job = getJob(uuid);

        SecurityContext context = SecurityContextHolder.getContext();
        job.setSecurityContext(context);

        logger.info("Job ready: {}", job.getUuid());
        job.setStatus(JobStatus.PENDING);
        jobQueue.add(job);
    }

    /**
     * Processes a job by running the compression service.
     * @param job the job to process
     */
    private void processJob(Job job) {
        SecurityContext previousContext = SecurityContextHolder.getContext(); // optional, for restoring later
        try {
            if (job.getSecurityContext() != null) {
                SecurityContextHolder.setContext(job.getSecurityContext());
            }

            clipService.create(
                    job.getInputVideoMetadata(),
                    job.getOutputVideoMetadata(),
                    job.getInputFile(),
                    job.getOutputFile(),
                    job.getProgress()
            );

            job.setStatus(JobStatus.FINISHED);

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Error while running job {}", job.getUuid(), e);

        } finally {
            // 🔄 Restore previous context to avoid leaking across jobs
            SecurityContextHolder.setContext(previousContext);
        }
    }

    /**
     * Starts the background processing loop in a daemon thread.
     * The loop blocks until a job is available and then processes it.
     */
    @PostConstruct
    private void startProcessingLoop() {
        Thread thread = new Thread(() -> {
            logger.info("Starting processing loop");
            while (true) {
                try {
                    Job job = jobQueue.take(); // Blocks until a job is available

                    logger.info("Starting job {}", job.getUuid());
                    job.setStatus(JobStatus.RUNNING);
                    processJob(job);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Processing loop interrupted", e);
                    break;
                }
            }
        });

        thread.setDaemon(true);
        thread.start();
    }
}