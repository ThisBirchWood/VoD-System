package com.ddf.vodsystem.controllers;

import com.ddf.vodsystem.services.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/v1/upload")
public class UploadController {

    private final UploadService uploadService;

    @Autowired
    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping()
    public ResponseEntity<String> uploadVideo(@RequestParam("file") MultipartFile file) {
        String uuid = uploadService.upload(file);

        return new ResponseEntity<>(uuid, HttpStatus.OK);
    }
}
