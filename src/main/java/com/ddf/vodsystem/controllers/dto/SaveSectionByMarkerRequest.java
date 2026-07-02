package com.ddf.vodsystem.controllers.dto;

import jakarta.validation.constraints.NotNull;

public record SaveSectionByMarkerRequest(
        @NotNull Long startMarkerId,
        @NotNull Long endMarkerId,
        String title,
        String description) {}
