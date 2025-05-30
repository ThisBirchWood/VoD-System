package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.Job;
import com.ddf.vodsystem.entities.JobStatus;
import com.ddf.vodsystem.exceptions.JobNotFound;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

@Service
public class JobService {
    private static final Logger logger = LoggerFactory.getLogger(JobService.class);
    private final HashMap<String, Job> jobs = new HashMap<>();
    private final LinkedList<Job> jobQueue = new LinkedList<>();
    private final CompressionService compressionService;

    public JobService(CompressionService compressionService) {
        this.compressionService = compressionService;
    }

    public void add(Job job) {
        logger.info("Added job: {}", job.getUuid());
        jobs.put(job.getUuid(), job);
    }

    public Job getJob(String uuid) {
        Job job = jobs.get(uuid);

        if (job == null) {
            throw new JobNotFound("Job not found");
        }

        return job;
    }

    public void jobReady(String uuid) {
        Job job = getJob(uuid);
        job.setProgress(0f);

        logger.info("Job ready: {}", job.getUuid());
        job.setStatus(JobStatus.PENDING);
        jobQueue.add(job);
    }

    @PostConstruct
    private void startProcessingLoop() {
        Thread thread = new Thread(() -> {
            logger.info("Starting processing loop");
            while (true) {
                if (!jobQueue.isEmpty()) {
                    Job job = jobQueue.poll();

                    logger.info("Starting job {}", job.getUuid());

                    try {
                        compressionService.run(job);// Execute the task
                    } catch (IOException | InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.error("Error while running job {}", job.getUuid(), e);
                    }
                }

            }
        });

        thread.setDaemon(true);
        thread.start();
    }
}
