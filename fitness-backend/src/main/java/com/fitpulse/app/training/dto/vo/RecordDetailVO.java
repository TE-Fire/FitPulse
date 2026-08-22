package com.fitpulse.app.training.dto.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 训练记录详情 VO（含组明细列表）。
 * <p>对应接口：GET /api/v1/training/records/{id}
 * <p>sets[] 按 exercise_id 分组内按 set_no 升序排列，便于前端按动作展示。
 */
@Data
@Builder
public class RecordDetailVO {

    /** 记录 ID */
    private Long id;

    /** 来源计划 ID（可空） */
    private Long planId;

    /** 来源计划名称（联查 workout_plan.name，可空） */
    private String planName;

    /** 训练日期（DATE 类型） */
    private LocalDate recordDate;

    /** 训练时长秒（可空） */
    private Integer durationSec;

    /** 总容量（B 维度重点） */
    private BigDecimal totalVolume;

    /** 总组数（B 维度重点） */
    private Integer totalSets;

    /** 总次数（B 维度重点） */
    private Integer totalReps;

    /** 训练备注（可空） */
    private String note;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 组明细列表（按 setNo 升序） */
    private List<RecordSetVO> sets;
}
