package com.ddf.vodsystem.exceptions;

public class JobNotFound extends RuntimeException {
    public JobNotFound(String message) {
        super(message);
    }
}
