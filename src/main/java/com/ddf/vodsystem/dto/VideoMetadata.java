package com.ddf.vodsystem.dto;

import lombok.Data;

@Data
public class VideoMetadata {
    private String title;
    private String description;
    private Float startPoint;
    private Float endPoint;
    private Float fps;
    private Integer width;
    private Integer height;
    private Float fileSize;
}
