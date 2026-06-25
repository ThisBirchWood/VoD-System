package com.ddf.vodsystem.controllers.dto;

import java.time.Instant;
import jakarta.validation.constraints.NotNull;

public record SaveSectionRequest(
        @NotNull Instant startTime,
        @NotNull Instant endTime) {}
