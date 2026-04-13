package com.auth.token.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

/**
 * PRD Redis 저장 패턴: rt:{token_value} → principalName:clientId:scope1,scope2
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenRedisRepository {

    private static final String RT_PREFIX = "rt:";
    private static final Duration RT_TTL = Duration.ofDays(7);

    private final StringRedisTemplate redisTemplate;

    public void save(String tokenValue, String principalName, String registeredClientId, Set<String> scopes) {
        String value = principalName + ":" + registeredClientId + ":" + String.join(",", scopes);
        redisTemplate.opsForValue().set(RT_PREFIX + tokenValue, value, RT_TTL);
    }

    public Optional<String> find(String tokenValue) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(RT_PREFIX + tokenValue));
    }

    public void delete(String tokenValue) {
        redisTemplate.delete(RT_PREFIX + tokenValue);
    }
}
