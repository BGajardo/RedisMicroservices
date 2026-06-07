package com.redis.AuthService.Service;

import com.redis.AuthService.DTO.AuthResponse;
import com.redis.AuthService.DTO.LoginRequest;
import com.redis.AuthService.DTO.RegisterRequest;
import com.redis.AuthService.Entity.RefreshToken;
import com.redis.AuthService.Entity.User;
import com.redis.AuthService.Exception.InvalidsCredentialsException;
import com.redis.AuthService.Exception.UserNotFoundException;
import com.redis.AuthService.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    private User user;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp(){
        user = new User();
        user.setUsername("Braulio");
        user.setPassword("Password");
        user.setRole("USER");

        refreshToken = new RefreshToken();
        refreshToken.setToken("token");
        refreshToken.setUsername("Braulio");
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshToken.setRevoked(false);

    }

    @Test
    void register_shouldSaveUser(){
        RegisterRequest req = new RegisterRequest();
        req.setUsername("Braulio");
        req.setPassword("pass123");

        when(passwordEncoder.encode("pass123")).thenReturn("encodedPassword");

        authService.register(req);

        verify(userRepository, times(1)).save(any(User.class));
    }


    @Test
    void login_shouldReturnTokens_whenCredentialsAreValid(){
        LoginRequest req = new LoginRequest();
        req.setUsername("Braulio");
        req.setPassword("pass123");

        when(userRepository.findByUsername("Braulio")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass123", "Password")).thenReturn(true);
        when(jwtService.generateToken("Braulio")).thenReturn("accessToken");
        when(jwtService.extractExpiration("accessToken")).thenReturn(900000L);
        when(refreshTokenService.create("Braulio")).thenReturn(refreshToken);

        AuthResponse response = authService.login(req);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals(refreshToken.getToken(), response.getRefreshToken());
    }

    @Test
    void login_shouldThrowUserNotFoundException_whenUserNotFound(){
        LoginRequest req = new LoginRequest();
        req.setUsername("UsuarioNoExistente");
        req.setPassword("pass123");
        when(userRepository.findByUsername("UsuarioNoExistente")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> authService.login(req));
    }

    @Test
    void login_shouldThrowInvalidCredentialsException_whenPasswordIsInvalid(){
        LoginRequest req = new LoginRequest();
        req.setUsername("Braulio");
        req.setPassword("claveErronea");
        when(userRepository.findByUsername("Braulio")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("claveErronea", "Password")).thenReturn(false);

        assertThrows(InvalidsCredentialsException.class, () -> authService.login(req));
    }


    @Test
    void logout_shouldBlacklistToken_andRemoveActiveToken(){
        when(jwtService.extractExpiration("accessToken")).thenReturn(900000L);
        when(jwtService.extractUsername("accessToken")).thenReturn("Braulio");

        authService.logout("accessToken", refreshToken.getToken());

        verify(tokenBlacklistService, times(1)).blacklist("accessToken", 900000L);
        verify(tokenBlacklistService, times(1)).removeActiveToken("Braulio");
        verify(refreshTokenService, times(1)).revoke("Braulio");
    }

    @Test
    void refreshToken_shouldReturnNewTokens(){
        when(refreshTokenService.validate(refreshToken.getToken())).thenReturn(refreshToken);
        when(jwtService.generateToken("Braulio")).thenReturn("newAccessToken");
        when(jwtService.extractExpiration("newAccessToken")).thenReturn(900000L);
        when(refreshTokenService.create("Braulio")).thenReturn(refreshToken);

        AuthResponse response = authService.refreshToken(refreshToken.getToken());

        assertNotNull(response);

        assertEquals("newAccessToken", response.getAccessToken());

    }


}
