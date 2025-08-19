package com.ddf.vodsystem.dto;

import lombok.Data;

@Data
public class ClipOptions {
    private String title;
    private String description;
    private Float startPoint;
    private Float duration;
    private Float fps;
    private Integer width;
    private Integer height;
    private Float fileSize;
}
