package com.fitpulse.app.training.dto.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 计划详情中的动作项 VO（联查 exercise.name）。
 * <p>targetReps 是 String（VARCHAR 32），不是数字。
 * <p>targetWeightKg 是 V3 追加字段（建议重量）。
 */
@Data
@Builder
public class PlanExerciseVO {

    /** 关联记录 ID */
    private Long id;

    /** 所属计划 ID */
    private Long planId;

    /** 动作 ID */
    private Long exerciseId;

    /** 动作名称（联查 exercise.name） */
    private String exerciseName;

    /** 排列顺序 */
    private Integer sortOrder;

    /** 目标组数 */
    private Integer targetSets;

    /** 目标次数（字符串，如 "8-12" / "力竭"） */
    private String targetReps;

    /** 建议重量 kg（可空） */
    private BigDecimal targetWeightKg;

    /** 组间休息秒（可空） */
    private Integer restSec;
}
