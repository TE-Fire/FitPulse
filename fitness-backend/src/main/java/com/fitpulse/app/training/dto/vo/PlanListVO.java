package com.fitpulse.app.training.dto.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 训练计划列表项 VO。
 * <p>planTypeLabel / statusText 由后端填充中文标签。
 * <p>exerciseCount 由后端联查 workout_plan_exercise 统计。
 */
@Data
@Builder
public class PlanListVO {

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

    /** 关联动作数量 */
    private Integer exerciseCount;

    /** 开始训练时间（V3 追加） */
    private LocalDateTime startedAt;

    /** 完成训练时间（V3 追加） */
    private LocalDateTime completedAt;

    /** 实际训练时长秒（V3 追加） */
    private Integer actualDurationSec;

    private LocalDateTime createdAt;
}
