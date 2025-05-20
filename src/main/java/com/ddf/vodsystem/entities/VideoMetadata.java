package com.ddf.vodsystem.entities;

import lombok.Data;

@Data
public class VideoMetadata {
    private Float startPoint;
    private Float endPoint;
    private Float fps;
    private Integer width;
    private Integer height;
    private Float fileSize;
}
