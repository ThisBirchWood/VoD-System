package com.ddf.vodsystem.dto;

import java.time.Instant;

public record SaveSectionRequest(Instant startTime, Instant endTime) {}
