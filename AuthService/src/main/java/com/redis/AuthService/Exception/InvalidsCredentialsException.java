package com.redis.AuthService.Exception;

public class InvalidsCredentialsException extends AuthException{
    public InvalidsCredentialsException() {
        super("Credenciales Invalidas", 401);
    }
}
