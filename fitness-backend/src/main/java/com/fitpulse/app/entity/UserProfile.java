package com.fitpulse.app.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户资料实体（对应 user_profile 表）。
 * <p>与 {@link User} 通过 user_id 一对一关联，存储用户的个人画像信息。
 * <p>V2 迁移新增字段：weight_kg / body_fat_pct / fitness_level / theme。
 *
 * @author FitPulse
 */
@Data
@TableName("user_profile")
public class UserProfile {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联用户 ID（等于 user.id）
     */
    private Long userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像文件 URL
     */
    private String avatarUrl;

    /**
     * 性别 0=未知 1=男 2=女
     */
    private Integer gender;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 身高 cm
     */
    private BigDecimal heightCm;

    /**
     * 当前体重 kg（缓存最新值，避免联表查 body_metric 历史）
     */
    private BigDecimal weightKg;

    /**
     * 当前体脂率 %（缓存最新值）
     */
    private BigDecimal bodyFatPct;

    /**
     * 健身等级 1=入门 2=进阶 3=达人 4=专业
     */
    private Integer fitnessLevel;

    /**
     * 主题偏好 1=浅色 2=深色 3=跟随系统
     */
    private Integer theme;

    /**
     * 个人简介
     */
    private String bio;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
