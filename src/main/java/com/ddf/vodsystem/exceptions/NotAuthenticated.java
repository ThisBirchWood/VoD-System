package com.ddf.vodsystem.exceptions;

public class NotAuthenticated extends RuntimeException {
    public NotAuthenticated(String message) {
        super(message);
    }
}
