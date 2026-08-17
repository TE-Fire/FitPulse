package com.fitpulse.app.auth.jwt;

import com.fitpulse.app.common.constants.RedisKeyConstants;
import com.fitpulse.app.common.enums.ErrorCodeEnum;
import com.fitpulse.app.common.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

/**
 * JWT 生成 / 解析 / 校验工具。
 * <p>依赖 jjwt 0.12.x，使用 HS256 对称签名。
 * <p>refreshToken 同时写入 Redis（key=fitpulse:auth:refresh:{userId}），用于服务端失效控制。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    private SecretKey key;

    @PostConstruct
    void init() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 accessToken
     *
     * @param userId   用户ID
     * @param username 用户名
     */
    public String generateAccessToken(Long userId, String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessExpireMinutes() * 60_000L);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * 生成 refreshToken 并写入 Redis（覆盖式，单设备登录语义）。
     */
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getRefreshExpireMinutes() * 60_000L);
        String token = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
        // 同步存 Redis，过期时间与 token 本身一致
        redisTemplate.opsForValue().set(
                RedisKeyConstants.buildRefreshTokenKey(String.valueOf(userId)),
                token,
                Duration.ofMinutes(jwtProperties.getRefreshExpireMinutes())
        );
        return token;
    }

    /**
     * 解析并校验 token（签名 + 过期），失败抛 BusinessException(UNAUTHORIZED)。
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.warn("[JWT] 解析失败: {}", e.getMessage());
            throw new BusinessException(ErrorCodeEnum.UNAUTHORIZED, "Token无效或已过期");
        }
    }

    /**
     * 从 token 中取 userId（sub claim）。
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.valueOf(claims.getSubject());
    }

    /**
     * 判断 refreshToken 在 Redis 中是否仍然有效（未被登出/旋转）。
     */
    public boolean isRefreshTokenValid(Long userId, String refreshToken) {
        Object stored = redisTemplate.opsForValue().get(
                RedisKeyConstants.buildRefreshTokenKey(String.valueOf(userId))
        );
        return stored != null && stored.equals(refreshToken);
    }

    /**
     * 删除 Redis 中的 refreshToken（登出 / 旋转失效）。
     */
    public void revokeRefreshToken(Long userId) {
        redisTemplate.delete(RedisKeyConstants.buildRefreshTokenKey(String.valueOf(userId)));
    }
}
