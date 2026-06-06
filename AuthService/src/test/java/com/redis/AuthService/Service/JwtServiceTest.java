package com.redis.AuthService.Service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JwtServiceTest {
    @Autowired
    private JwtService jwtService;

    private static final String USERNAME = "Braulio";

    @Test
    void generateToken_shouldReturnToken(){
        String token = jwtService.generateToken(USERNAME);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername(){
        String token = jwtService.generateToken(USERNAME);
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals(USERNAME, extractedUsername);
    }

    @Test
    void isTokenValid_shouldReturnTrue_whenTokenIsValid(){
        String token = jwtService.generateToken(USERNAME);
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsInvalid(){
        assertFalse(jwtService.isTokenValid("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30"));
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsEmpty(){
        assertFalse(jwtService.isTokenValid(""));
    }

    @Test
    void extractExpiration_shouldReturnPositiveValue(){
        String token = jwtService.generateToken(USERNAME);
        assertTrue(jwtService.extractExpiration(token) > 0);
    }

    @Test
    void extractExpiration_shouldReturnLessThan15Minutes(){
        String token = jwtService.generateToken(USERNAME);
        assertTrue(jwtService.extractExpiration(token) <= 1000 * 60 * 15);
    }





}
