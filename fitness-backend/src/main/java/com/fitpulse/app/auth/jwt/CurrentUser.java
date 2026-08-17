package com.fitpulse.app.auth.jwt;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 工具类：从 SecurityContext 获取当前登录用户
 */
public class CurrentUser {
    public static JwtAuthFilter.LoginUser get() {
        SecurityContext ctx = SecurityContextHolder.getContext();
        if (ctx == null || ctx.getAuthentication() == null) return null;
        Object p = ctx.getAuthentication().getPrincipal();
        if (p instanceof JwtAuthFilter.LoginUser u) return u;
        return null;
    }
    public static Long userId() {
        JwtAuthFilter.LoginUser u = get();
        return u == null ? null : u.userId();
    }
    public static Long requireUserId() {
        Long id = userId();
        if (id == null) throw new com.fitpulse.app.common.exception.BusinessException(
                com.fitpulse.app.common.result.ResultCode.UNAUTHORIZED);
        return id;
    }
}