package com.redis.gateway.Config;

import com.redis.gateway.Security.Filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder){
        return builder.routes()
                .route("auth-public", r -> r.path("/auth/login", "/auth/register").uri("http://auth-service:8081"))
                .route("auth-private", r -> r.path("/auth/logout","/auth/profile", "/auth/refresh").filters(f -> f.filter(jwtAuthFilter)).uri("http://auth-service:8081"))
                .route("data-service", r -> r.path("/api/products/**").filters(f -> f.filter(jwtAuthFilter)).uri("http://data-service:8082"))
                .build();
    }

}
