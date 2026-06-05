package com.redis.AuthService.Exception;

import com.redis.AuthService.DTO.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(new ErrorResponse(ex.getStatus(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        return ResponseEntity
                .status(500)
                .body(new ErrorResponse(500, "Error Interno del Servidor"));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex){
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField()+ ": "+ error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity
                .status(400)
                .body(new ErrorResponse(400, message));
    }


}
