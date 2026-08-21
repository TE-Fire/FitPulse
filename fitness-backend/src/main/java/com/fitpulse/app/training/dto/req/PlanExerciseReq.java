package com.fitpulse.app.training.dto.req;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 计划内嵌动作请求（PlanCreateReq / PlanUpdateReq 的子元素）。
 * <p>target_reps 是 VARCHAR(32) 字符串，支持 "8-12" / "力竭" / "12,10,8" 等格式。
 * <p>rest_sec 字段名是 rest_sec（不是 rest_seconds）。
 * <p>target_weight_kg 是 V3 追加字段，用于预填建议重量。
 */
@Data
public class PlanExerciseReq {

    /** 动作 ID（必须存在） */
    @NotNull(message = "动作ID不能为空")
    private Long exerciseId;

    /** 排列顺序（≥1） */
    @NotNull(message = "排列顺序不能为空")
    @Min(value = 1, message = "排列顺序必须≥1")
    private Integer sortOrder;

    /** 目标组数（1-99） */
    @NotNull(message = "目标组数不能为空")
    @Min(value = 1, message = "目标组数必须≥1")
    @Max(value = 99, message = "目标组数不能超过99")
    private Integer targetSets;

    /** 目标次数（字符串，如 "8-12" / "力竭"） */
    @NotNull(message = "目标次数不能为空")
    @Size(max = 32, message = "目标次数长度不能超过32")
    private String targetReps;

    /** 建议重量 kg（0-999） */
    @DecimalMin(value = "0", message = "建议重量不能为负数")
    @DecimalMax(value = "999", message = "建议重量不能超过999")
    private BigDecimal targetWeightKg;

    /** 组间休息秒（0-600） */
    @Min(value = 0, message = "组间休息秒不能为负数")
    @Max(value = 600, message = "组间休息秒不能超过600")
    private Integer restSec;
}
