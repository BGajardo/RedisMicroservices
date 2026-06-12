package com.redis.AuthService.Controller;

import com.redis.AuthService.DTO.AuthResponse;
import com.redis.AuthService.DTO.LoginRequest;
import com.redis.AuthService.DTO.RefreshRequest;
import com.redis.AuthService.DTO.RegisterRequest;
import com.redis.AuthService.Service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Endpoints de autenticacion")
public class AuthController {


    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }



    @Operation(summary = "Registrar nuevo usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario Registrado"),
            @ApiResponse(responseCode = "400", description = "Datos Invalidos")
    })
    @PostMapping("/register")
    public void register(@RequestBody @Valid RegisterRequest req){
        authService.register(req);
    }


    @Operation(summary = "Iniciar sesión")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
            @ApiResponse(responseCode = "429", description = "Demasiados intentos")
    })
    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest req){
        return authService.login(req);
    }

    @Operation(summary = "Cerrar Sesion")
    @ApiResponse(responseCode = "200", description = "Logout exitoso")
    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String authHeader, @RequestBody @Valid RefreshRequest req){
        String accessToken = authHeader.replace("Bearer ", "");
        authService.logout(accessToken, req.getRefreshToken());
    }

    @Operation(summary = "Renovar Tokens")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens Renovados"),
            @ApiResponse(responseCode = "401", description = "Refresh Token invalido")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody @Valid RefreshRequest req){
        return ResponseEntity.ok(authService.refreshToken(req.getRefreshToken()));
    }

    @Operation(summary = "Obtener Perfil de Usuario")
    @ApiResponse(responseCode = "200", description = "Perfil del usuario")
    @GetMapping("/profile")
    public String profile(Authentication authentication){
        return "Authenticated user: "+authentication.getName();
    }
}
