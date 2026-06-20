package com.ddf.vodsystem.controllers.dto;

public record StreamResponse(
        boolean isStreaming,
        Long id
) {
}
