package com.ddf.vodsystem.controllers.dto;

import com.ddf.vodsystem.dto.JobState;

import java.time.Instant;

public record JobResponse (
        String uuid,
        float progress,
        boolean isComplete,
        JobState state,
        String errorOutput,
        Instant createdAt
) {
}
