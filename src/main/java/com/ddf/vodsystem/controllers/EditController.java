package com.ddf.vodsystem.controllers;

import com.ddf.vodsystem.dto.VideoMetadata;
import com.ddf.vodsystem.services.EditService;
import lombok.AllArgsConstructor;
import lombok.Data;
import com.ddf.vodsystem.dto.APIResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/v1")
public class EditController {
    private final EditService editService;
    private static final String SUCCESS = "success";

    public EditController(EditService editService) {
        this.editService = editService;
    }

    @PostMapping("edit/{uuid}")
    public ResponseEntity<APIResponse<Void>> edit(@PathVariable("uuid") String uuid, @ModelAttribute VideoMetadata videoMetadata) {
        editService.edit(uuid, videoMetadata);
        return ResponseEntity.ok(new APIResponse<>(SUCCESS, "Editing started for UUID: " + uuid, null));
    }

    @GetMapping("/process/{uuid}")
    public ResponseEntity<APIResponse<Void>> convert(@PathVariable("uuid") String uuid) {
        editService.process(uuid);
        return ResponseEntity.ok(new APIResponse<>(SUCCESS, "Processing started for UUID: " + uuid, null));
    }

    @GetMapping("/progress/{uuid}")
    public ResponseEntity<APIResponse<ProgressResponse>> getProgress(@PathVariable("uuid") String uuid) {
        float progress = editService.getProgress(uuid);

        ProgressResponse progressResponse = new ProgressResponse(progress);
        return ResponseEntity.ok(new APIResponse<>(SUCCESS, "Progress for UUID: " + uuid, progressResponse));
    }

    @Data
    @AllArgsConstructor
    public static class ProgressResponse {
        private float progress;
    }
}