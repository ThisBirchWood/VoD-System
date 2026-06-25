package com.ddf.vodsystem.controllers.dto;

import java.time.Instant;

public record ClipResponse(
        Long id,
        Long userId,
        String title,
        String description,
        Float duration,
        Instant createdAt
) {}