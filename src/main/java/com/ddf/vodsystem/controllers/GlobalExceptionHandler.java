package com.ddf.vodsystem.controllers;

import com.ddf.vodsystem.exceptions.JobNotFinished;
import com.ddf.vodsystem.exceptions.JobNotFound;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({ MultipartException.class })
    public ResponseEntity<String> handleMultipartException(MultipartException ex) {
        return ResponseEntity.badRequest().body("Request is not multipart/form-data.");
    }

    @ExceptionHandler({ MissingServletRequestPartException.class })
    public ResponseEntity<String> handleMissingPart(MissingServletRequestPartException ex) {
        return ResponseEntity.badRequest().body("Required file part is missing.");
    }

    @ExceptionHandler({ HttpMediaTypeNotSupportedException.class })
    public ResponseEntity<String> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body("Unsupported media type: expected multipart/form-data.");
    }

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
