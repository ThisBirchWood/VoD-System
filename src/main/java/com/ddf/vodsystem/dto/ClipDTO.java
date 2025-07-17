package com.ddf.vodsystem.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClipDTO {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private Float duration;
    private LocalDateTime createdAt;
}
