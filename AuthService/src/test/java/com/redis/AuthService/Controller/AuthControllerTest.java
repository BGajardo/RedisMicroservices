package com.redis.AuthService.Controller;

import com.redis.AuthService.DTO.AuthResponse;
import com.redis.AuthService.DTO.LoginRequest;
import com.redis.AuthService.DTO.RefreshRequest;
import com.redis.AuthService.DTO.RegisterRequest;
import com.redis.AuthService.Exception.InvalidsCredentialsException;
import com.redis.AuthService.Exception.UserNotFoundException;
import com.redis.AuthService.Service.AuthService;
import com.redis.AuthService.Service.AuthServiceTest;
import com.redis.AuthService.Service.JwtService;
import com.redis.AuthService.Service.TokenBlacklistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;


    @Test
    @WithMockUser
    void register_shouldReturn200() throws Exception{
        RegisterRequest req = new RegisterRequest();
        req.setUsername("Braulio");
        req.setPassword("pass123");

        mockMvc.perform(post("/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(authService, times(1)).register(req);

    }

    @Test
    @WithMockUser
    void register_shouldReturn400_whenBodyIsNotValid() throws Exception{
        RegisterRequest req = new RegisterRequest();
        req.setUsername("");
        req.setPassword("123456");

        mockMvc.perform(post("/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }


    @Test
    @WithMockUser
    void login_shouldReturn200_withCredentialsAreValid() throws Exception{
        LoginRequest req = new LoginRequest();
        req.setUsername("Braulio");
        req.setPassword("pass123");

        when(authService.login(any())).thenReturn(new AuthResponse("access-token", "refresh-token"));



        mockMvc.perform(post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }


    @Test
    @WithMockUser
    void login_shouldReturn404_whenBodyIsNotValid() throws Exception{
        LoginRequest req = new LoginRequest();
        req.setUsername("noexiste");
        req.setPassword("123456");

        when(authService.login(any())).thenThrow(new UserNotFoundException("Usuario no existe"));

        mockMvc.perform(post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));

    }

    @Test
    @WithMockUser
    void login_shouldReturn401_whenCredentialsAreInvalid() throws Exception{
        LoginRequest req = new LoginRequest();
        req.setUsername("Braulio");
        req.setPassword("wrongPassword");

        when(authService.login(any())).thenThrow(new InvalidsCredentialsException());

        mockMvc.perform(post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser
    void refresh_shouldReturn200_whenTokenIsValid() throws Exception{
        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken("refresh-token");

        when(authService.refreshToken(any())).thenReturn(new AuthResponse("new-access-token", "new-refresh-token"));

        mockMvc.perform(post("/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }


    @Test
    @WithMockUser
    void logout_shouldReturn200_whenTokenIsValid() throws Exception{
        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken("refresh-token");

        mockMvc.perform(post("/auth/logout")
                        .with(csrf())
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                        .andExpect(status().isOk());


        verify(authService, times(1)).logout(any(), any());
    }




}
