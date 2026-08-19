package com.fitpulse.app.user.dto.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户完整资料响应（聚合 user + user_profile 联查）。
 * <p>对应接口：GET /api/v1/user/profile
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVO {

    /** 用户 ID */
    private Long userId;

    /** 用户名（注册时由邮箱前缀自动生成，只读） */
    private String username;

    /** 邮箱 */
    private String email;

    /** 手机号（可空） */
    private String phone;

    /** 账号状态 1=启用 0=禁用 */
    private Integer status;

    /** 最后登录时间（可空） */
    private LocalDateTime lastLoginAt;

    /** 注册时间 */
    private LocalDateTime createdAt;

    /** 用户资料对象（user_profile 表） */
    private Profile profile;

    /**
     * user_profile 表字段聚合（V2 新增字段已标注）。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Profile {

        /** 昵称 */
        private String nickname;

        /** 头像文件 URL */
        private String avatarUrl;

        /** 性别 0=未知 1=男 2=女 */
        private Integer gender;

        /** 生日 */
        private LocalDate birthday;

        /** 身高 cm */
        private BigDecimal heightCm;

        /** 当前体重 kg（V2 新增，缓存最新值） */
        private BigDecimal weightKg;

        /** 当前体脂率 %（V2 新增，缓存最新值） */
        private BigDecimal bodyFatPct;

        /** 健身等级 1=入门 2=进阶 3=达人 4=专业（V2 新增） */
        private Integer fitnessLevel;

        /** 主题偏好 1=浅色 2=深色 3=跟随系统（V2 新增） */
        private Integer theme;

        /** 个人简介 */
        private String bio;
    }
}
