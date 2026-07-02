package com.ddf.vodsystem.controllers.dto;

import java.time.Instant;

public record MarkerResponse(
        Long id,
        Long userId,
        Long streamId,
        String message,
        Instant timestamp
) {}
