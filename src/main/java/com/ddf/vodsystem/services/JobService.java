package com.ddf.vodsystem.services;

import com.ddf.vodsystem.tools.Job;
import com.ddf.vodsystem.entities.JobStatus;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;

@Service
public class JobService {
    private static final Logger logger = LoggerFactory.getLogger(JobService.class);
    private final HashMap<String, Job> jobs = new HashMap<>();
    private final LinkedList<Job> jobQueue = new LinkedList<>();

    public void add(Job job) {
        logger.info("Added job: {}", job.getUuid());
        jobs.put(job.getUuid(), job);
    }

    public Job get(String uuid) {
        return jobs.get(uuid);
    }

    public void jobReady(String uuid) {
        Job job = jobs.get(uuid);

        if (job == null) {
            throw new RuntimeException("Job not found");
        }

        logger.info("Job ready: {}", job.getUuid());
        job.setStatus(JobStatus.PENDING);
        jobQueue.add(job);
    }

    @PostConstruct
    private void startProcessingLoop() {
        Thread thread = new Thread(() -> {
            while (true) {
                if (!jobQueue.isEmpty()) {
                    Job task = jobQueue.poll();

                    logger.info("Starting job {}", task.getUuid());
                    task.run(); // Execute the task
                }

            }
        });

        thread.setDaemon(true);
        thread.start();
    }
}
