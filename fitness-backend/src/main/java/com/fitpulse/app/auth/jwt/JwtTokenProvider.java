package com.fitpulse.app.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtProperties props;
    private final StringRedisTemplate redis;

    private static final String REDIS_KEY = "fitpulse:token:";

    private SecretKey key() {
        return Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId, String username) {
        long exp = props.getAccessExpireMinutes() * 60_000;
        String token = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + exp))
                .signWith(key())
                .compact();
        // Redis 存一份，登出/踢人用
        redis.opsForValue().set(REDIS_KEY + userId + ":access:" + token, "1",
                props.getAccessExpireMinutes(), TimeUnit.MINUTES);
        return token;
    }

    public String createRefreshToken(Long userId) {
        long exp = props.getRefreshExpireMinutes() * 60_000;
        String token = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + exp))
                .signWith(key())
                .compact();
        redis.opsForValue().set(REDIS_KEY + userId + ":refresh:" + token, "1",
                props.getRefreshExpireMinutes(), TimeUnit.MINUTES);
        return token;
    }

    public Claims parse(String token) {
        try {
            return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isTokenValidInRedis(Long userId, String token) {
        return Boolean.TRUE.equals(redis.hasKey(REDIS_KEY + userId + ":access:" + token));
    }

    public void invalidateAccessToken(Long userId, String token) {
        redis.delete(REDIS_KEY + userId + ":access:" + token);
    }
}
