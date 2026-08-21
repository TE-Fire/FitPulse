package com.fitpulse.app.training.dto.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 当前进行中训练 VO。
 * <p>对应接口：GET /api/v1/training/plans/in-progress
 * <p>前端使用：页面加载调用此接口，有活动则进入训练中页面，
 * 计时器从 elapsedSec 秒继续。
 */
@Data
@Builder
public class InProgressVO {

    /** 是否有进行中的训练 */
    private Boolean hasActivePlan;

    /** 计划 ID（无活动时 null） */
    private Long planId;

    /** 计划名称（可空） */
    private String name;

    /** 开始时间（可空） */
    private LocalDateTime startedAt;

    /** 已经过秒数（后端根据 startedAt 计算，可空） */
    private Integer elapsedSec;

    /** 完整计划详情（含 exercises[]，仅 hasActivePlan=true 时返回） */
    private PlanDetailVO plan;
}
