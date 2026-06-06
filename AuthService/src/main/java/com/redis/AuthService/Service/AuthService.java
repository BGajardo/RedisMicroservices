package com.redis.AuthService.Service;

import com.redis.AuthService.DTO.AuthResponse;
import com.redis.AuthService.DTO.LoginRequest;
import com.redis.AuthService.DTO.RegisterRequest;
import com.redis.AuthService.Entity.RefreshToken;
import com.redis.AuthService.Entity.User;
import com.redis.AuthService.Exception.InvalidsCredentialsException;
import com.redis.AuthService.Exception.UserNotFoundException;
import com.redis.AuthService.Repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Slf4j
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
        log.info("Registrando un nuevo usuario: {}", req.getUsername());
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole("USER");
        userRepository.save(user);
        log.info("Usuario registrado con exito: {}", req.getUsername());
    }

    public AuthResponse login(LoginRequest req){
        log.info("Intentando iniciar sesion de usuario: {}", req.getUsername());
        User user = userRepository.findByUsername(req.getUsername()).orElseThrow(() -> {
            log.warn("Inicio de sesion fallado, usuario no encontrado : {}", req.getUsername());
            return new UserNotFoundException(req.getUsername());
        });
        if(!passwordEncoder.matches(req.getPassword(), user.getPassword())){
            log.warn("Inicio de sesion fallado, credenciales invalidas: {}", req.getUsername());
            throw new InvalidsCredentialsException();
        }
        String accessToken =  jwtService.generateToken(user.getUsername());
        String refreshToken = refreshTokenService.create(user.getUsername()).getToken();

        long expiration = jwtService.extractExpiration(accessToken);
        tokenBlacklistService.saveActiveToken(user.getUsername(), accessToken, expiration);
        log.info("Inicio de sesion exitoso: {}", req.getUsername());
        return new AuthResponse(accessToken, refreshToken);
    }


    public AuthResponse refreshToken(String refreshToken){
        RefreshToken token = refreshTokenService.validate(refreshToken);
        log.info("Token refresh validado: {}", token.getUsername());
        String accessToken = jwtService.generateToken(token.getUsername());

        long expiration = jwtService.extractExpiration(accessToken);
        tokenBlacklistService.saveActiveToken(token.getUsername(), accessToken, expiration);

        String newRefreshToken = refreshTokenService.create(token.getUsername()).getToken();
        log.info("Token refresh generado nuevamente: {}", token.getUsername());
        return new AuthResponse(accessToken, newRefreshToken);
    }



    public void logout(String accessToken, String refreshToken){
       long expiration = jwtService.extractExpiration(accessToken);
        String username = jwtService.extractUsername(accessToken);
       log.info("Cerrando sesion de usuario: {}", username);
       tokenBlacklistService.blacklist(accessToken, expiration);
       tokenBlacklistService.removeActiveToken(username);
       refreshTokenService.revoke(username);
       log.info("Cerrando sesion exitosa: {}", username);
    }



}
