package com.fitpulse.app.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 计划动作关联实体（对应 workout_plan_exercise 表）。
 * <p>注意：表名是 workout_plan_exercise（不是 workout_plan_item）。
 * <p>target_reps 是 VARCHAR(32) 字符串，支持 "8-12" / "力竭" / "12,10,8" 等格式。
 * <p>rest_sec 字段名为 rest_sec（不是 rest_seconds）。
 * <p>V3 追加字段：target_weight_kg（计划建议默认重量）。
 * <p>此表无 updated_at / deleted 字段，仅有 created_at。
 *
 * @author FitPulse
 */
@Data
@TableName("workout_plan_exercise")
public class WorkoutPlanExercise {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属计划 ID */
    private Long planId;

    /** 关联动作 ID */
    private Long exerciseId;

    /** 动作顺序 */
    private Integer sortOrder;

    /** 目标组数 */
    private Integer targetSets;

    /** 目标次数（字符串，如 "8-12" / "力竭" / "12,10,8"） */
    private String targetReps;

    /** 组间休息秒 */
    private Integer restSec;

    /** 计划建议重量 kg，用户录入时可快速填充（V3 追加） */
    private java.math.BigDecimal targetWeightKg;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
