package com.fitpulse.app.training.dto.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 训练计划详情 VO（含关联动作列表）。
 * <p>对应接口：GET /api/v1/training/plans/{id}
 */
@Data
@Builder
public class PlanDetailVO {

    private Long id;
    private String name;

    /** 1=力量 2=有氧 3=混合 */
    private Integer planType;
    private String planTypeLabel;

    /** 0=草稿 1=进行中 2=已完成 3=已取消 */
    private Integer status;
    private String statusText;

    private String description;
    private Integer estimatedMin;

    /** 开始训练时间（V3 追加） */
    private LocalDateTime startedAt;

    /** 完成训练时间（V3 追加） */
    private LocalDateTime completedAt;

    /** 实际训练时长秒（V3 追加） */
    private Integer actualDurationSec;

    /** 关联动作列表（按 sortOrder 排序） */
    private List<PlanExerciseVO> exercises;
}
