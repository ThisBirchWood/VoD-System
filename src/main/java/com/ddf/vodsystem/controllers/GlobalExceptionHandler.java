package com.ddf.vodsystem.controllers;

import com.ddf.vodsystem.dto.APIResponse;
import com.ddf.vodsystem.exceptions.FFMPEGException;
import com.ddf.vodsystem.exceptions.JobNotFinished;
import com.ddf.vodsystem.exceptions.JobNotFound;
import com.ddf.vodsystem.exceptions.NotAuthenticated;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String ERROR = "error";

    @ExceptionHandler({ MultipartException.class })
    public ResponseEntity<APIResponse<Void>> handleMultipartException(MultipartException ex) {
        logger.error("MultipartException: {}", ex.getMessage());
        APIResponse<Void> response = new APIResponse<>(ERROR, "Multipart request error: " + ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler({ MissingServletRequestPartException.class })
    public ResponseEntity<APIResponse<Void>> handleMissingPart(MissingServletRequestPartException ex) {
        logger.error("MissingServletRequestPartException: {}", ex.getMessage());
        APIResponse<Void> response = new APIResponse<>(ERROR, "Required file part is missing.", null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler({ HttpMediaTypeNotSupportedException.class })
    public ResponseEntity<APIResponse<Void>> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        logger.error("HttpMediaTypeNotSupportedException: {}", ex.getMessage());
        APIResponse<Void> response = new APIResponse<>(ERROR, "Unsupported media type: expected multipart/form-data.", null);
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<APIResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        logger.error("IllegalArgumentException: {}", ex.getMessage());
        APIResponse<Void> response = new APIResponse<>(ERROR, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(JobNotFound.class)
    public ResponseEntity<APIResponse<Void>> handleFileNotFound(JobNotFound ex) {
        logger.error("JobNotFound: {}", ex.getMessage());
        APIResponse<Void> response = new APIResponse<>(ERROR, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(JobNotFinished.class)
    public ResponseEntity<APIResponse<Void>> handleJobNotFinished(JobNotFinished ex) {
        logger.error("JobNotFinished: {}", ex.getMessage());
        APIResponse<Void> response = new APIResponse<>(ERROR, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @ExceptionHandler(FFMPEGException.class)
    public ResponseEntity<APIResponse<Void>> handleFFMPEGException(FFMPEGException ex) {
        logger.error("FFMPEGException: {}", ex.getMessage());
        APIResponse<Void> response = new APIResponse<>(ERROR, "FFMPEG Error: Please upload a valid file", null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(NotAuthenticated.class)
    public ResponseEntity<APIResponse<Void>> handleNotAuthenticated(NotAuthenticated ex) {
        logger.error("NotAuthenticated: {}", ex.getMessage());
        APIResponse<Void> response = new APIResponse<>(ERROR, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}