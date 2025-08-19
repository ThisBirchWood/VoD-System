package com.ddf.vodsystem.controllers;

import com.ddf.vodsystem.dto.JobStatus;
import com.ddf.vodsystem.dto.options.ClipOptions;
import com.ddf.vodsystem.services.EditService;
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
    public ResponseEntity<APIResponse<Void>> edit(@PathVariable("uuid") String uuid, @ModelAttribute ClipOptions clipOptions) {
        editService.edit(uuid, clipOptions);
        return ResponseEntity.ok(new APIResponse<>(SUCCESS, "Editing started for UUID: " + uuid, null));
    }

    @GetMapping("/process/{uuid}")
    public ResponseEntity<APIResponse<Void>> process(@PathVariable("uuid") String uuid) {
        editService.process(uuid);
        return ResponseEntity.ok(new APIResponse<>(SUCCESS, "Processing started for UUID: " + uuid, null));
    }

    @GetMapping("/convert/{uuid}")
    public ResponseEntity<APIResponse<Void>> convert(@PathVariable("uuid") String uuid) {
        editService.convert(uuid);
        return ResponseEntity.ok(new APIResponse<>(SUCCESS, "Conversion started for UUID: " + uuid, null));
    }

    @GetMapping("/progress/{uuid}")
    public ResponseEntity<APIResponse<JobStatus>> getProgress(@PathVariable("uuid") String uuid) {
        JobStatus status = editService.getProgress(uuid);
        return ResponseEntity.ok(new APIResponse<>(SUCCESS, "Progress for UUID: " + uuid, status));
    }
}