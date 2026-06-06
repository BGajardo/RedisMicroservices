package com.redis.gateway.Security.Filter;


import com.redis.gateway.Service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GatewayFilter {

    private final JwtService jwtService;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain){
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

        // publicas
        if(path.startsWith("/auth/register") || path.startsWith("/auth/login") || path.startsWith("/auth/refresh")){
            log.debug("Path publica, omitiendo validacion JWT: {}", path);
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            log.warn("Header Authorization ausente o invalido: {}", path);
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        if(!jwtService.isTokenValid(token)){
            log.warn("Token invalido para la ruta: {}", path);
            return unauthorized(exchange);
        }

        return redisTemplate.hasKey("blacklist:"+token)
                .flatMap(isBlacklisted -> {
                    if(isBlacklisted){
                        log.warn("Intento con token en blacklist para la ruta : {}", path);
                        return unauthorized(exchange);
                    }

                    String username = jwtService.extractUsername(token);

                    return  redisTemplate.opsForValue().get("jwt:"+ username)
                            .defaultIfEmpty("")
                            .flatMap(activeToken -> {
                                if(!activeToken.equals(token)){
                                    log.warn("Token no activo para el usuario para la ruta: {}", path);
                                    return unauthorized(exchange);
                                }
                                log.info("Request autorizada para usuario: {} ruta: {}", username, path);
                                ServerHttpRequest mutatedRequest = request.mutate().header("X-User-Name", username).build();
                                return chain.filter(exchange.mutate().request(mutatedRequest).build());
                            });
                });

    }


    private Mono<Void> unauthorized(ServerWebExchange exchange){
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = """
                {
                "status": 401,
                "message": "Unauthorized",
                "timestamp" : "%s"
                }
                """.formatted(Instant.now().toString());

        DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(body.getBytes());

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

}
