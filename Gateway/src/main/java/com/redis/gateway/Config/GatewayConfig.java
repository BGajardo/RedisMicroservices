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
                .route("auth-public", r -> r.path("/auth/login", "/auth/register", "/auth/refresh").uri("http://auth-service:8081"))
                .route("auth-private", r -> r.path("/auth/logout","/auth/profile").filters(f -> f.filter(jwtAuthFilter)).uri("http://auth-service:8081"))
                .route("data-service", r -> r.path("/api/products/**").filters(f -> f.filter(jwtAuthFilter)).uri("http://data-service:8082"))
                .route("auth-health", r -> r.path("/auth/health").filters(f -> f.rewritePath("/auth/health", "/actuator/health")).uri("http://auth-service:8081"))
                .route("data-health", r -> r.path("/data/health").filters(f -> f.rewritePath("/data/health", "/actuator/health")).uri("http://data-service:8082"))
                .build();
    }

}
