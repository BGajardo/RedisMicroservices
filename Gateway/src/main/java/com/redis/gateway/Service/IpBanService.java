package com.redis.gateway.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class IpBanService {

    private final ReactiveRedisTemplate<String, String> redisTemplate;


    private static final String BAN_PREFIX = "banned:";
    private static final String ATTEMPTS_PREFIX  = "attempts:";
    private static final int MAX_ATTEMPTS = 5;
    private static final int BAN_MINUTES = 15;

    public Mono<Boolean> isBanned(String ip){
        return redisTemplate.hasKey(BAN_PREFIX + ip);
    }


    public Mono<Void> registerFailedAttempt(String ip){
        String key = ATTEMPTS_PREFIX + ip;
        return redisTemplate.opsForValue().increment(key)
                .flatMap(attempts -> {
                   log.warn("Intento fallido #{} para IP: {}", attempts, ip);



                   if(attempts == 1){
                       return redisTemplate.expire(key, Duration.ofMinutes(BAN_MINUTES)).then();
                   }

                   if(attempts >= MAX_ATTEMPTS){
                       log.warn("IP baneada por {} minutos {}", BAN_MINUTES, ip);
                       return redisTemplate.opsForValue()
                               .set(BAN_PREFIX + ip, "banned", Duration.ofMinutes(BAN_MINUTES))
                               .then(redisTemplate.delete(key))
                               .then();
                   }
                    return Mono.empty();
                });
    }


    public Mono<Void> clearAttempts(String ip){
        return redisTemplate.delete(ATTEMPTS_PREFIX + ip).then();
    }


}
