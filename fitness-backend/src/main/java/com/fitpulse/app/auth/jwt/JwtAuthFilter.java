package com.fitpulse.app.auth.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器：从请求头解析 accessToken，成功则把 userId/username 写入 SecurityContext。
 * <p>解析失败不抛异常，仅清空上下文放行，由后续 Security 链决定是否放行该请求。
 * <p>约定：principal = userId(Long)，details = username(String)。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(jwtProperties.getHeader());
        String prefix = jwtProperties.getPrefix();

        // 无 Token 或格式不符，直接放行
        if (!StringUtils.hasText(header) || !header.startsWith(prefix)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(prefix.length()).trim();
        try {
            Claims claims = jwtTokenProvider.parseToken(token);
            // 仅 access 类型 token 可用于鉴权
            if (!"access".equals(claims.get("type"))) {
                log.warn("[JWT] 非 accessToken 类型，拒绝鉴权");
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }
            Long userId = Long.valueOf(claims.getSubject());
            String username = claims.get("username", String.class);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
            auth.setDetails(username);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception e) {
            // 解析失败：清空上下文，放行交给 Security 链处理（未登录访问受保护接口会被 401）
            log.debug("[JWT] 鉴权失败: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }
}
