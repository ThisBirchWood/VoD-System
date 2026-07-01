package com.ddf.vodsystem.controllers.dto;

import java.time.Instant;

public record VodResponse(
        Long id,
        Long userId,
        String title,
        String description,
        Float duration,
        Instant createdAt
) {}
