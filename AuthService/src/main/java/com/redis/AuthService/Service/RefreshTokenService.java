package com.redis.AuthService.Service;

import com.redis.AuthService.Entity.RefreshToken;
import com.redis.AuthService.Exception.InvalidTokenException;
import com.redis.AuthService.Repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
@Transactional
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken create(String username) {
        refreshTokenRepository.deleteByUsername(username);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUsername(username);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshToken.setRevoked(false);
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken validate(String token){
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Refresh Token Invalido"));

        if(refreshToken.isRevoked()){
            throw new InvalidTokenException("Refresh Token Revocado");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())){
            throw new InvalidTokenException("Refresh Token Expirado");
        }

        return refreshToken;
    }

    public void revoke(String username){
        refreshTokenRepository.deleteByUsername(username);
    }


}
