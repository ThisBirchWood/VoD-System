package com.ddf.vodsystem.exceptions;

public class NotStreaming extends RuntimeException {
    public NotStreaming(String message) {
        super(message);
    }
}
