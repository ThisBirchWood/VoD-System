package com.ddf.vodsystem.controllers;

import com.ddf.vodsystem.entities.ClipConfig;
import com.ddf.vodsystem.services.EditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class EditController {
    private final EditService editService;

    @Autowired
    public EditController(EditService editService) {
        this.editService = editService;
    }

    @PostMapping("edit/{uuid}")
    public ResponseEntity<String> edit(@PathVariable("uuid") String uuid, @ModelAttribute ClipConfig clipConfig) {
        editService.edit(uuid, clipConfig);
        return new ResponseEntity<>(uuid, HttpStatus.OK);
    }

    @GetMapping("/process/{uuid}")
    public ResponseEntity<String> convert(@PathVariable("uuid") String uuid) {
        editService.jobReady(uuid);
        return new ResponseEntity<>(uuid, HttpStatus.OK);
    }

    @GetMapping("/progress/{uuid}")
    public ResponseEntity<Float> getProgress(@PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(editService.getProgress(uuid), HttpStatus.OK);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }

}
