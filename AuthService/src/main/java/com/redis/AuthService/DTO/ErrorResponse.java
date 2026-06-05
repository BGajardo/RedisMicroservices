package com.redis.AuthService.DTO;

import lombok.Data;

import java.time.Instant;

@Data
public class ErrorResponse {

    private int status;
    private String message;
    private String timestamp;

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = Instant.now().toString();
    }

}
