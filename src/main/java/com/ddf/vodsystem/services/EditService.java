package com.ddf.vodsystem.services;

import com.ddf.vodsystem.dto.JobStatus;
import com.ddf.vodsystem.dto.ClipOptions;
import com.ddf.vodsystem.dto.Job;
import com.ddf.vodsystem.services.media.MetadataService;
import org.springframework.stereotype.Service;

@Service
public class EditService {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EditService.class);
    private final MetadataService metadataService;
    private final JobRegistryService jobRegistryService;
    private final JobOrchestrationService jobOrchestrationService;

    public EditService(JobOrchestrationService jobOrchestrationService,
                       MetadataService metadataService,
                       JobRegistryService jobRegistryService) {
        this.metadataService = metadataService;
        this.jobRegistryService = jobRegistryService;
        this.jobOrchestrationService = jobOrchestrationService;

    }

    public void edit(String uuid, ClipOptions clipOptions) {
        Job job = jobRegistryService.getJob(uuid);
        metadataService.validateMetadata(job.getInputClipOptions(), clipOptions);
        job.setOutputClipOptions(clipOptions);

        logger.info("Job {} - Updated clip config to {}", job.getUuid(), clipOptions);
    }

    public void process(String uuid) {
        Job job = jobRegistryService.getJob(uuid);
        jobOrchestrationService.processJob(job);

        logger.info("Job {} - Started processing", uuid);
    }

    public void convert(String uuid) {
        Job job = jobRegistryService.getJob(uuid);
        jobOrchestrationService.convertJob(job);

        logger.info("Job {} - Started converting", uuid);
    }

    public JobStatus getStatus(String uuid) {
        Job job = jobRegistryService.getJob(uuid);
        return job.getStatus();
    }
}
