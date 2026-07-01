package com.ddf.vodsystem.exceptions;

public class VodNotFound extends RuntimeException {
    public VodNotFound(String message) {
        super(message);
    }
}
