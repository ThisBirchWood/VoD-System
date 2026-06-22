package com.ddf.vodsystem.controllers;

import com.ddf.vodsystem.controllers.dto.UUIDResponse;
import com.ddf.vodsystem.dto.Job;
import com.ddf.vodsystem.dto.ClipOptions;
import com.ddf.vodsystem.dto.APIResponse;
import com.ddf.vodsystem.services.MediaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {
    private final MediaService mediaService;
    private static final String SUCCESS = "success";

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping("/compress")
    public ResponseEntity<APIResponse<UUIDResponse>> compress(
            @RequestParam("file")MultipartFile file,
            @RequestParam("options") ClipOptions options
            ) throws IOException, InterruptedException {
        Job job = mediaService.compress(file, options);
        return ResponseEntity.ok(new APIResponse<>(
                SUCCESS,
                "Compression successfully started",
                new UUIDResponse(job.getUuid())
        ));
    }


    @PostMapping("/save")
    public ResponseEntity<APIResponse<UUIDResponse>> save(
            @RequestParam("start") Instant startTime,
            @RequestParam("end") Instant endTime) throws IOException {
        Job job = mediaService.saveSection(startTime, endTime);
        return ResponseEntity.ok(new APIResponse<>(
                SUCCESS,
                "Section saving successfully started",
                new UUIDResponse(job.getUuid())
        ));
    }

    @PostMapping("/clip")
    public ResponseEntity<APIResponse<UUIDResponse>> clip(
            @RequestParam("duration") float duration) throws IOException {
        Job job = mediaService.clip(duration);
        return ResponseEntity.ok(new APIResponse<>(
                SUCCESS,
                "Clipping successfully started",
                new UUIDResponse(job.getUuid())
        ));
    }
}