package com.ddf.vodsystem.services;

import com.ddf.vodsystem.dto.JobStatus;
import com.ddf.vodsystem.dto.ClipOptions;
import com.ddf.vodsystem.dto.Job;
import com.ddf.vodsystem.services.media.MetadataService;
import org.springframework.stereotype.Service;

@Service
public class EditService {
    private final JobService jobService;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EditService.class);
    private final MetadataService metadataService;

    public EditService(JobService jobService, MetadataService metadataService) {
        this.jobService = jobService;
        this.metadataService = metadataService;
    }

    public void edit(String uuid, ClipOptions clipOptions) {
        Job job = jobService.getJob(uuid);
        metadataService.validateMetadata(job.getInputClipOptions(), clipOptions);
        job.setOutputClipOptions(clipOptions);

        logger.info("Job {} - Updated clip config to {}", job.getUuid(), clipOptions);
    }

    public void process(String uuid) {
        Job job = jobService.getJob(uuid);
        jobService.processJob(job);

        logger.info("Job {} - Started processing", uuid);
    }

    public void convert(String uuid) {
        Job job = jobService.getJob(uuid);
        jobService.convertJob(job);

        logger.info("Job {} - Started converting", uuid);
    }

    public JobStatus getStatus(String uuid) {
        Job job = jobService.getJob(uuid);
        return job.getStatus();
    }
}
