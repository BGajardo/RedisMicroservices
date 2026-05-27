package com.redis.AuthService.DTO;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
}
