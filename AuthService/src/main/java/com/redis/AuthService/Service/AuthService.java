package com.redis.AuthService.Service;

import com.redis.AuthService.DTO.AuthResponse;
import com.redis.AuthService.DTO.LoginRequest;
import com.redis.AuthService.DTO.RegisterRequest;
import com.redis.AuthService.Entity.RefreshToken;
import com.redis.AuthService.Entity.User;
import com.redis.AuthService.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;


    public AuthService(UserRepository userRepository, JwtService jwtService, TokenBlacklistService tokenBlacklistService, PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    public void register(RegisterRequest req){
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole("USER");
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest req){
        User user = userRepository.findByUsername(req.getUsername()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if(!passwordEncoder.matches(req.getPassword(), user.getPassword())){
            throw new RuntimeException("Credenciales invalidas");
        }
        String accessToken =  jwtService.generateToken(user.getUsername());
        String refreshToken = refreshTokenService.create(user.getUsername()).getToken();
        return new AuthResponse(accessToken, refreshToken);
    }


    public AuthResponse refreshToken(String refreshToken){
        RefreshToken token = refreshTokenService.validate(refreshToken);
        String accessToken = jwtService.generateToken(token.getUsername());
        return new AuthResponse(accessToken, refreshToken);
    }



    public void logout(String accessToken, String refreshToken){
       long expiration = jwtService.extractExpiration(accessToken);
       tokenBlacklistService.blacklist(accessToken, expiration);
       String username = jwtService.extractUsername(accessToken);
       refreshTokenService.revoke(username);
    }



}
