package com.ddf.vodsystem.controllers;

import com.ddf.vodsystem.exceptions.JobNotFinished;
import com.ddf.vodsystem.exceptions.JobNotFound;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(400).body(ex.getMessage());
    }

    @ExceptionHandler(JobNotFound.class)
    public ResponseEntity<String> handleFileNotFound(JobNotFound ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }

    @ExceptionHandler(JobNotFinished.class)
    public ResponseEntity<String> handleJobNotFinished(JobNotFinished ex) {
        return ResponseEntity.status(202).body(ex.getMessage());
    }
}
