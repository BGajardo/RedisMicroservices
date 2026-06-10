package com.redis.gateway.Config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;
@Slf4j
@Configuration
public class RateLimiterConfig {

    @Primary
    @Bean
    public RedisRateLimiter defaultRateLimiter(){
        return new RedisRateLimiter(1, 30, 1);
    }

    @Bean
    public RedisRateLimiter loginRateLimiter(){
        return new RedisRateLimiter(1,5,1);
    }

    @Bean
    public RedisRateLimiter registerRateLimiter(){
        return new RedisRateLimiter(1, 3, 1);
    }

    @Bean
    public RedisRateLimiter apiRateLimiter(){
        return new RedisRateLimiter(1,60, 1);
    }


    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {

            String ip = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
            log.info("Rate Limiter - IP Detectada: {}", ip);
            return Mono.just(ip);
        };
    }
}
