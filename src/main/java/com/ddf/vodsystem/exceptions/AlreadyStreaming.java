package com.ddf.vodsystem.exceptions;

public class AlreadyStreaming extends RuntimeException {
    public AlreadyStreaming(String message) {
        super(message);
    }
}
