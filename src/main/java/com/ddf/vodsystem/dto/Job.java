package com.ddf.vodsystem.dto;

import java.io.File;
import lombok.Data;

@Data
public class Job {
    private String uuid;
    private File inputFile;
    private File outputFile;

    // configs
    private ClipMetadata inputClipMetadata;
    private ClipMetadata outputClipMetadata = new ClipMetadata();

    // job status
    private JobStatus status = new JobStatus();

    public Job(String uuid,
               File inputFile,
               File outputFile,
               ClipMetadata inputClipMetadata) {
        this.uuid = uuid;
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.inputClipMetadata = inputClipMetadata;
    }
}
