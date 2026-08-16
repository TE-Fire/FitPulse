package com.fitpulse.app.auth.jwt;

/**
 * 工具类：从 SecurityContext 获取当前登录用户
 */
public class CurrentUser {
    public static JwtAuthFilter.LoginUser get() {
        var ctx = org.springframework.security.core.context.SecurityContextHolder.getContext();
        if (ctx == null || ctx.getAuthentication() == null) return null;
        var p = ctx.getAuthentication().getPrincipal();
        if (p instanceof JwtAuthFilter.LoginUser u) return u;
        return null;
    }
    public static Long userId() {
        var u = get(); return u == null ? null : u.userId();
    }
    public static Long requireUserId() {
        Long id = userId();
        if (id == null) throw new com.fitpulse.app.common.exception.BusinessException(
                com.fitpulse.app.common.result.ResultCode.UNAUTHORIZED);
        return id;
    }
}
