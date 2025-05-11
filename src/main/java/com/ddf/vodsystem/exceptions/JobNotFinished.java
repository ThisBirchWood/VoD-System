package com.ddf.vodsystem.exceptions;

public class JobNotFinished extends RuntimeException {
    public JobNotFinished(String message) {
        super(message);
    }
}
