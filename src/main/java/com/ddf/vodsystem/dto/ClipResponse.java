package com.ddf.vodsystem.dto;

import java.time.LocalDateTime;

public record ClipResponse(
        Long id,
        Long userId,
        String title,
        String description,
        Float duration,
        LocalDateTime createdAt
) {}