package com.redis.gateway.Security.Filter;

import com.redis.gateway.Service.IpBanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class IPBanFilter implements GatewayFilter {

    private final IpBanService ipBanService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String ip = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        return ipBanService.isBanned(ip)
                .flatMap(isBanned -> {
                    if (isBanned) {
                        log.warn("IP baneada intentando acceder: {}", ip);
                        return banned(exchange);
                    }

                    return chain.filter(exchange).then(
                            Mono.defer(() -> {
                                var statusCode = exchange.getResponse().getStatusCode();
                                if (statusCode == null) return Mono.empty();
                                int code = statusCode.value();
                                if (code == 401) {
                                    return ipBanService.registerFailedAttempt(ip);
                                } else if (code == 200) {
                                    return ipBanService.clearAttempts(ip);
                                }
                                return Mono.empty();
                            })
                    );
                });
    }

    private Mono<Void> banned(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = """
            {
                "status": 429,
                "message": "IP bloqueada temporalmente, intente en 15 minutos",
                "timestamp": "%s"
            }
            """.formatted(java.time.Instant.now().toString());
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes());
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}