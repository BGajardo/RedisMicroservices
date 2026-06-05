package com.redis.AuthService.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "El username no puede quedar vacio")
    @Size(min = 3, max = 20, message = "El username debe tener entre 3 y 20 caracteres")
    private String username;
    @NotBlank(message = "La contraseña no puede quedar vacio")
    @Size(min = 6, message = "La contraseña debe tener minimo 6 caracteres")
    private String password;
}
