package com.redis.gateway.Config;

import com.redis.gateway.Security.Filter.IPBanFilter;
import com.redis.gateway.Security.Filter.JwtAuthFilter;
import com.redis.gateway.Service.IpBanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Log4j2
@Configuration
public class GatewayConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final KeyResolver ipKeyResolver;

    private final RedisRateLimiter loginRateLimiter;
    private final RedisRateLimiter registerRateLimiter;
    private final RedisRateLimiter apiRateLimiter;
    private final IpBanService ipBanService;
    private final IPBanFilter ipBanFilter;

    public GatewayConfig(
            JwtAuthFilter jwtAuthFilter,
            KeyResolver ipKeyResolver,
            IPBanFilter ipBanFilter,
            @Qualifier("loginRateLimiter") RedisRateLimiter loginRateLimiter,
            @Qualifier("registerRateLimiter") RedisRateLimiter registerRateLimiter,
            @Qualifier("apiRateLimiter") RedisRateLimiter apiRateLimiter,
            IpBanService ipBanService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.ipKeyResolver = ipKeyResolver;
        this.loginRateLimiter = loginRateLimiter;
        this.registerRateLimiter = registerRateLimiter;
        this.apiRateLimiter = apiRateLimiter;
        this.ipBanService = ipBanService;
        this.ipBanFilter = ipBanFilter;
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder){
        return builder.routes()

                .route("auth-login", r -> r
                        .path("/auth/login")
                        .filters(f -> f
                                .filter(ipBanFilter)
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(loginRateLimiter)
                                        .setKeyResolver(ipKeyResolver)
                                        .setDenyEmptyKey(false)
                                )

                        ).uri("http://auth-service:8081")
                )
                .route("auth-register", r -> r
                        .path("/auth/register")
                        .filters(f -> f
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(registerRateLimiter)
                                        .setKeyResolver(ipKeyResolver)
                                        .setDenyEmptyKey(false)
                                ))
                        .uri("http://auth.service:8081")
                )
                .route("auth-public", r -> r.path("/auth/refresh").uri("http://auth-service:8081"))
                .route("auth-private", r -> r.path("/auth/logout","/auth/profile").filters(f -> f.filter(jwtAuthFilter)).uri("http://auth-service:8081"))
                .route("data-service", r -> r
                        .path("/api/products/**")
                        .filters(f -> f
                                .filter(jwtAuthFilter)
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(apiRateLimiter)
                                        .setKeyResolver(ipKeyResolver)
                                        .setDenyEmptyKey(false)
                                )
                        ).uri("http://data-service:8082"))
                .route("auth-health", r -> r.path("/auth/health").filters(f -> f.rewritePath("/auth/health", "/actuator/health")).uri("http://auth-service:8081"))
                .route("data-health", r -> r.path("/data/health").filters(f -> f.rewritePath("/data/health", "/actuator/health")).uri("http://data-service:8082"))
                .build();
    }

}
