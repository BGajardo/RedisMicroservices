package com.redis.AuthService.Controller;

import com.redis.AuthService.DTO.AuthResponse;
import com.redis.AuthService.DTO.LoginRequest;
import com.redis.AuthService.DTO.RegisterRequest;
import com.redis.AuthService.Service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {


    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public void register(@RequestBody RegisterRequest req){
        authService.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req){
        String token = authService.login(req);
        return new AuthResponse(token);
    }
    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String authHeader){
        String token = authHeader.replace("Bearer ", "");
        authService.logout(token);
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication){
        return "Authenticated user: "+authentication.getName();
    }
}
