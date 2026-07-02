package com.ddf.vodsystem.controllers.dto;

import jakarta.validation.constraints.NotBlank;

public record MarkerCreateRequest(
        @NotBlank String message
) {}
