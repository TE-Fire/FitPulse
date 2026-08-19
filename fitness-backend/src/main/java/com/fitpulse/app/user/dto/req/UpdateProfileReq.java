package com.fitpulse.app.user.dto.req;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 更新基本资料请求（仅更新 user_profile 表字段，部分更新语义）。
 * <p>对应接口：PUT /api/v1/user/profile
 * <p>所有字段均可空，仅更新请求体中非 null 的字段。
 */
@Data
public class UpdateProfileReq {

    /** 昵称（1-64 字符） */
    @Size(max = 64, message = "昵称长度不能超过64")
    private String nickname;

    /** 性别 0=未知 1=男 2=女 */
    @Min(value = 0, message = "性别值非法")
    @Max(value = 2, message = "性别值非法")
    private Integer gender;

    /** 生日 */
    private LocalDate birthday;

    /** 身高 cm（50-300） */
    @DecimalMin(value = "50", message = "身高不能小于50cm")
    @DecimalMax(value = "300", message = "身高不能大于300cm")
    private BigDecimal heightCm;

    /** 当前体重 kg（20-500） */
    @DecimalMin(value = "20", message = "体重不能小于20kg")
    @DecimalMax(value = "500", message = "体重不能大于500kg")
    private BigDecimal weightKg;

    /** 当前体脂率 %（3-60） */
    @DecimalMin(value = "3", message = "体脂率不能小于3%")
    @DecimalMax(value = "60", message = "体脂率不能大于60%")
    private BigDecimal bodyFatPct;

    /** 健身等级 1=入门 2=进阶 3=达人 4=专业 */
    @Min(value = 1, message = "健身等级值非法")
    @Max(value = 4, message = "健身等级值非法")
    private Integer fitnessLevel;

    /** 主题偏好 1=浅色 2=深色 3=跟随系统 */
    @Min(value = 1, message = "主题偏好值非法")
    @Max(value = 3, message = "主题偏好值非法")
    private Integer theme;

    /** 个人简介（≤512 字符） */
    @Size(max = 512, message = "个人简介长度不能超过512")
    private String bio;
}
