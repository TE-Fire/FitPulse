package com.fitpulse.app.training.dto.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 训练记录列表项 VO。
 * <p>planName 由后端联查 workout_plan.name（可空，保留未来非计划直录场景）。
 * <p>三个 B 维度重点指标：totalVolume / totalSets / totalReps 均以记录级统计字段为准。
 */
@Data
@Builder
public class RecordListVO {

    /** 记录 ID */
    private Long id;

    /** 来源计划 ID（可空） */
    private Long planId;

    /** 来源计划名称（联查 workout_plan.name，可空） */
    private String planName;

    /** 训练日期（yyyy-MM-dd，DATE 类型） */
    private LocalDate recordDate;

    /** 训练时长秒（可空） */
    private Integer durationSec;

    /** 总容量 = Σ(weight × reps)，热身组不参与（B 维度重点） */
    private BigDecimal totalVolume;

    /** 总组数（B 维度重点） */
    private Integer totalSets;

    /** 总次数（B 维度重点） */
    private Integer totalReps;

    /** 训练备注（可空） */
    private String note;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
