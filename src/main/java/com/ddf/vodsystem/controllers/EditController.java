package com.ddf.vodsystem.controllers;

import com.ddf.vodsystem.entities.VideoMetadata;
import com.ddf.vodsystem.services.EditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/")
public class EditController {
    private final EditService editService;

    @Autowired
    public EditController(EditService editService) {
        this.editService = editService;
    }

    @PostMapping("edit/{uuid}")
    public ResponseEntity<String> edit(@PathVariable("uuid") String uuid, @ModelAttribute VideoMetadata videoMetadata) {
        editService.edit(uuid, videoMetadata);
        return new ResponseEntity<>(uuid, HttpStatus.OK);
    }

    @GetMapping("/process/{uuid}")
    public ResponseEntity<String> convert(@PathVariable("uuid") String uuid) {
        editService.process(uuid);
        return new ResponseEntity<>(uuid, HttpStatus.OK);
    }

    @GetMapping("/progress/{uuid}")
    public ResponseEntity<Float> getProgress(@PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(editService.getProgress(uuid), HttpStatus.OK);
    }

}
