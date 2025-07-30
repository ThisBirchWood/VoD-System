package com.ddf.vodsystem.entities;

import com.ddf.vodsystem.dto.ProgressTracker;
import lombok.Data;

@Data
public class JobStatus {
    private ProgressTracker processTracker = new ProgressTracker();
    private ProgressTracker remuxTracker = new ProgressTracker();
}


