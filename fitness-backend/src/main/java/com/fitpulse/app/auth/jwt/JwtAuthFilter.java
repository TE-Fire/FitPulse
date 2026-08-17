package com.fitpulse.app.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitpulse.app.common.result.Result;
import com.fitpulse.app.common.result.ResultCode;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwt;
    private final JwtProperties props;
    private final StringRedisTemplate redis;
    private final ObjectMapper om;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException {
        String auth = req.getHeader(props.getHeader());
        if (auth != null && auth.startsWith(props.getPrefix())) {
            String token = auth.substring(props.getPrefix().length());
            Claims claims = jwt.parse(token);
            if (claims == null) { writeUnauthorized(resp, "Token无效"); return; }
            Long userId;
            try { userId = Long.valueOf(claims.getSubject()); }
            catch (Exception e) { writeUnauthorized(resp, "Token无效"); return; }
            if (!jwt.isTokenValidInRedis(userId, token)) { writeUnauthorized(resp, "登录已过期"); return; }
            String username = claims.get("username", String.class);
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    new LoginUser(userId, username), null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
        chain.doFilter(req, resp);
    }

    private void writeUnauthorized(HttpServletResponse resp, String msg) throws IOException {
        resp.setStatus(401);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(om.writeValueAsString(Result.fail(ResultCode.UNAUTHORIZED, msg)));
    }

    public record LoginUser(Long userId, String username) {}
}