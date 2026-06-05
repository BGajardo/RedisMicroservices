package com.redis.gateway.Config;


import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
@Order(-1)
public class GlobalErrorHandler implements WebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex){
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Error interno del servidor";

        if(ex instanceof ResponseStatusException rse){
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            message = rse.getReason() != null ? rse.getReason() : message;
        }else if (isConnectionError(ex)) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            message = "Servicio no disponible temporalmente";
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            Map<String, Object> body = Map.of(
                    "status", status.value(),
                    "message", message,
                    "timestamp", java.time.Instant.now().toString()
            );
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return Mono.error(e);
        }

    }


    private boolean isConnectionError(Throwable ex) {
        if (ex instanceof java.net.ConnectException) return true;
        if (ex instanceof java.net.UnknownHostException) return true;
        if (ex.getCause() instanceof java.net.ConnectException) return true;
        if (ex.getCause() instanceof java.net.UnknownHostException) return true;
        if (ex.getMessage() != null && ex.getMessage().contains("Connection refused")) return true;
        if (ex.getMessage() != null && ex.getMessage().contains("Failed to resolve")) return true;
        return false;
    }



}
