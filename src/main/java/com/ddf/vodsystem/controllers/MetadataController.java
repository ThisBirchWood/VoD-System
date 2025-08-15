package com.ddf.vodsystem.controllers;

import com.ddf.vodsystem.dto.ClipMetadata;
import com.ddf.vodsystem.dto.APIResponse;
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
    public ResponseEntity<APIResponse<ClipMetadata>> getMetadata(@PathVariable String uuid) {
        ClipMetadata originalMetadata = jobService.getJob(uuid).getInputClipMetadata();

        if (originalMetadata == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new APIResponse<>("error", "Original metadata not found", null));
        }

        return ResponseEntity.ok()
                .body(new APIResponse<>("success", "Original metadata retrieved", originalMetadata));
    }

    @GetMapping("/converted/{uuid}")
    public ResponseEntity<APIResponse<ClipMetadata>> getConvertedMetadata(@PathVariable String uuid) {
        ClipMetadata convertedMetadata = jobService.getJob(uuid).getOutputClipMetadata();

        if (convertedMetadata == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new APIResponse<>("error", "Converted metadata not found", null));
        }

        return ResponseEntity.ok()
                .body(new APIResponse<>("success", "Converted metadata retrieved", convertedMetadata));
    }
}
