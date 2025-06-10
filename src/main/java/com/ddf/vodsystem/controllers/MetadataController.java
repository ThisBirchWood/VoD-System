package com.ddf.vodsystem.controllers;

import com.ddf.vodsystem.entities.VideoMetadata;
import com.ddf.vodsystem.entities.APIResponse;
import com.ddf.vodsystem.services.JobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/metadata")
public class MetadataController {
    private final JobService jobService;

    public MetadataController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/original/{uuid}")
    public ResponseEntity<APIResponse<VideoMetadata>> getMetadata(@PathVariable String uuid) {
        VideoMetadata originalMetadata = jobService.getJob(uuid).getInputVideoMetadata();

        if (originalMetadata == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new APIResponse<>("error", "Original metadata not found", null));
        }

        return ResponseEntity.ok()
                .body(new APIResponse<>("success", "Original metadata retrieved", originalMetadata));
    }

    @GetMapping("/converted/{uuid}")
    public ResponseEntity<APIResponse<VideoMetadata>> getConvertedMetadata(@PathVariable String uuid) {
        VideoMetadata convertedMetadata = jobService.getJob(uuid).getOutputVideoMetadata();

        if (convertedMetadata == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new APIResponse<>("error", "Converted metadata not found", null));
        }

        return ResponseEntity.ok()
                .body(new APIResponse<>("success", "Converted metadata retrieved", convertedMetadata));
    }
}
