package com.fitpulse.app.auth.jwt;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录用户工具类。
 * <p>约定（由 JwtAuthFilter 写入）：
 * <ul>
 *   <li>principal = userId(Long)</li>
 *   <li>details   = username(String)</li>
 * </ul>
 * 未登录时返回 null，调用方自行处理（Controller 受保护接口理论上不会出现 null）。
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    /**
     * 获取当前登录用户ID，未登录返回 null。
     */
    public static Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof Long)) {
            return null;
        }
        return (Long) auth.getPrincipal();
    }

    /**
     * 获取当前登录用户名，未登录返回 null。
     */
    public static String getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getDetails() instanceof String)) {
            return null;
        }
        return (String) auth.getDetails();
    }
}
