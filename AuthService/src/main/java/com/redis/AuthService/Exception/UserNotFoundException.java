package com.redis.AuthService.Exception;

public class UserNotFoundException extends AuthException{
    public UserNotFoundException(String username) {
        super("Usuario no Encontrado: " + username, 401);
    }
}
