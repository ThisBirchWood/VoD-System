package com.ddf.vodsystem.dto.options;

import lombok.Data;

import java.util.List;

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

    // Audio options
    private Boolean flattenAudio = false;
    private List<AudioOptions> audioOptions;
}
