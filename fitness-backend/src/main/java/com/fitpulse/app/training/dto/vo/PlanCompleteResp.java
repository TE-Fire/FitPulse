package com.fitpulse.app.training.dto.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 完成训练响应。
 * <p>对应接口：POST /api/v1/training/plans/{id}/complete
 * <p>后端自动计算 totalVolume/totalSets/totalReps 并写入 workout_record。
 */
@Data
@Builder
public class PlanCompleteResp {

    /** 生成的训练记录 ID */
    private Long recordId;

    /** 计划 ID */
    private Long planId;

    /** 训练日期（yyyy-MM-dd） */
    private LocalDate recordDate;

    /** 训练时长秒 */
    private Integer durationSec;

    /** 总容量 = Σ(weight × reps)，热身组不参与 */
    private BigDecimal totalVolume;

    /** 总组数 */
    private Integer totalSets;

    /** 总次数 */
    private Integer totalReps;
}
