package com.redis.AuthService.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshRequest {
    @NotBlank(message = "El refresh token no puede estar vacio")
    private String refreshToken;
}
