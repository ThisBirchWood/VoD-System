package com.ddf.vodsystem.dto;

import lombok.Data;

@Data
public class JobStatus {
    private ProgressTracker process = new ProgressTracker();
    private ProgressTracker conversion = new ProgressTracker();
}


