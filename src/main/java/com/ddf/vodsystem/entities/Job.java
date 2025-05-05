package com.ddf.vodsystem.entities;

import lombok.Data;
import java.io.File;

@Data
public class Job implements Runnable {
    private String uuid;
    private File file;
    private boolean started;
    private float progress;

    // configs
    private float startPoint;
    private float endPoint;
    private float fps;
    private int width;
    private int height;

    public Job(String uuid, File file) {
        this.uuid = uuid;
        this.file = file;
        this.started = false;
    }

    @Override
    public void run() {
        this.started = true;
        this.progress = 0;

        System.out.println("<UNK>");
    }
}
