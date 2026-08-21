package com.fitpulse.app.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 训练组明细实体（对应 workout_set 表）。
 * <p>记录每组的实际训练数据，在完成训练时由系统批量插入。
 * <p>注意：此表没有 rest_sec 字段（与 workout_plan_exercise 不同）。
 * <p>is_completed / is_warmup 为 TINYINT（1/0），不是 Boolean。
 * <p>此表无 updated_at / deleted 字段，仅有 created_at。
 *
 * @author FitPulse
 */
@Data
@TableName("workout_set")
public class WorkoutSet {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联 workout_record.id */
    private Long recordId;

    /** 关联动作 ID */
    private Long exerciseId;

    /** 第 N 组（TINYINT，1-127） */
    private Integer setNo;

    /** 负重量 kg（DECIMAL 8,2，自重动作可空） */
    private BigDecimal weightKg;

    /** 完成次数（力竭可空） */
    private Integer reps;

    /** 1=完成 0=未完成（TINYINT，不是 Boolean） */
    private Integer isCompleted;

    /** 1=热身组 0=非热身（TINYINT，不是 Boolean） */
    private Integer isWarmup;

    /** RPE 评分 1-10（TINYINT） */
    private Integer rpe;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
