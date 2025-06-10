package com.ddf.vodsystem.entities;

import lombok.Data;

@Data
public class APIResponse<T> {
    private String status;
    private String message;
    private T data;

    public APIResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
