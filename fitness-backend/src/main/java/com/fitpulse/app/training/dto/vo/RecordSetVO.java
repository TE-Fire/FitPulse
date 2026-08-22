package com.fitpulse.app.training.dto.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 记录详情中的组明细 VO。
 * <p>exerciseName 由后端联查 exercise.name。
 * <p>isCompleted / isWarmup 从 workout_set 的 TINYINT(1/0) 转换为 Boolean，
 * 因为 VO 层以语义化表达为主（存储层与展示层解耦）。
 */
@Data
@Builder
public class RecordSetVO {

    /** 组明细 ID */
    private Long id;

    /** 动作 ID */
    private Long exerciseId;

    /** 动作名称（联查 exercise.name） */
    private String exerciseName;

    /** 第 N 组（TINYINT，1-127） */
    private Integer setNo;

    /** 重量 kg（DECIMAL 8,2，自重可空） */
    private BigDecimal weightKg;

    /** 完成次数（力竭可空） */
    private Integer reps;

    /** 是否完成（由 TINYINT 1/0 转换） */
    private Boolean isCompleted;

    /** 是否热身组（由 TINYINT 1/0 转换） */
    private Boolean isWarmup;

    /** RPE 评分 1-10（可空） */
    private Integer rpe;
}
