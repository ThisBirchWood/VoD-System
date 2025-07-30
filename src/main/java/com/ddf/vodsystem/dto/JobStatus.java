package com.ddf.vodsystem.dto;

import lombok.Data;

@Data
public class JobStatus {
    private ProgressTracker processTracker = new ProgressTracker();
    private ProgressTracker remuxTracker = new ProgressTracker();
}


