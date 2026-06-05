package com.redis.AuthService.Service;

import com.redis.AuthService.Exception.InvalidTokenException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String ACTIVE_PREFIX = "jwt:";

    private final RedisTemplate<String, String> redisTemplate;

    public TokenBlacklistService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String buildKey(String token){
        return BLACKLIST_PREFIX + token;
    }

    private String buildActiveKey(String username){
        return ACTIVE_PREFIX + username;
    }


    public void blacklist(String token, long expirationMillis){
        if(token == null || token.isBlank()){
            throw new InvalidTokenException("Token no puede ser nulo o vacio");
        }
        if(expirationMillis <= 0){
            throw new InvalidTokenException("La expiracion debe ser positiva");
        }
        redisTemplate.opsForValue().set(buildKey(token), Instant.now().toString(), Duration.ofMillis(expirationMillis));
    }

    public boolean isBlacklisted(String token){
        if (token == null || token.isBlank()) return false;
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(token)));
    }

    public void saveActiveToken(String username, String token, long expirationMillis){
        redisTemplate.opsForValue().set(
            buildActiveKey(username), token, Duration.ofMillis(expirationMillis)
        );
    }

    public String getActiveToken(String username){
        return redisTemplate.opsForValue().get(buildActiveKey(username));
    }

    public void removeActiveToken(String username){
        redisTemplate.delete(buildActiveKey(username));
    }

}
