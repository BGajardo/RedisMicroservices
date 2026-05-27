package com.redis.AuthService.Service;

import com.redis.AuthService.DTO.LoginRequest;
import com.redis.AuthService.DTO.RegisterRequest;
import com.redis.AuthService.Entity.User;
import com.redis.AuthService.Repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final PasswordEncoder passwordEncoder;


    public AuthService(UserRepository userRepository, JwtService jwtService, TokenBlacklistService tokenBlacklistService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequest req){
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole("USER");
        userRepository.save(user);
    }

    public String login(LoginRequest req){
        User user = userRepository.findByUsername(req.getUsername()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if(!passwordEncoder.matches(req.getPassword(), user.getPassword())){
            throw new RuntimeException("Credenciales invalidas");
        }
        return jwtService.generateToken(user.getUsername());
    }

    public void logout(String token){
       long expiration = 15 * 60 * 1000;
       tokenBlacklistService.blacklist(token, expiration);
    }



}
