package com.ddf.vodsystem.services;

import com.ddf.vodsystem.dto.Job;
import com.ddf.vodsystem.exceptions.JobNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store for {@link Job} instances, keyed by job UUID.
 * Provides add/lookup only — actual job processing is handled by
 * {@link JobOrchestrationService}.
 */
@Service
public class JobRegistryService {
    private static final Logger logger = LoggerFactory.getLogger(JobRegistryService.class);
    private final ConcurrentHashMap<String, Job> jobs = new ConcurrentHashMap<>();

    /**
     * Adds a new job to the job map.
     *
     * @param job the job to add
     */
    public void add(Job job) {
        logger.info("Added job: {}", job.getUuid());
        jobs.put(job.getUuid(), job);
    }

    /**
     * Retrieves a job by its UUID.
     *
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
}