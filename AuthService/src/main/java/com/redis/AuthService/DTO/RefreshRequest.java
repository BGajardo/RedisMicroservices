package com.redis.AuthService.DTO;

import lombok.Data;

@Data
public class RefreshRequest {
    private String refreshToken;
}
