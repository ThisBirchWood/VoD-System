package com.ddf.vodsystem.controllers.input;

import jakarta.validation.constraints.NotNull;

public record ClipSectionRequest(
        @NotNull float duration
) {}