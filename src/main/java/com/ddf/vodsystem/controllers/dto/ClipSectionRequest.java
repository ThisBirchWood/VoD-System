package com.ddf.vodsystem.controllers.dto;

import jakarta.validation.constraints.NotNull;

public record ClipSectionRequest(
        @NotNull float duration,
        String title,
        String description
) {}