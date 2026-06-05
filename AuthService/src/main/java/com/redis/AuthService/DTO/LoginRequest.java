package com.redis.AuthService.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "El username no puede estar vacio")
    private String username;
    @NotBlank(message = "La Contraseña no puede estar vacia")
    @Size(min = 6, message = "La Contraseña debe tener minimo 6 caracteres")
    private String password;
}
