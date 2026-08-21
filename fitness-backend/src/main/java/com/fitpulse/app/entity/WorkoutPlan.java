package com.fitpulse.app.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 训练计划实体（对应 workout_plan 表）。
 * <p>计划为训练模板，包含状态机流转：DRAFT→IN_PROGRESS→COMPLETED/CANCELLED。
 * <p>V3 追加字段：status / started_at / completed_at / actual_duration_sec。
 *
 * @author FitPulse
 */
@Data
@TableName("workout_plan")
public class WorkoutPlan {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 归属用户 ID */
    private Long userId;

    /** 计划名称，如"推日A" */
    private String name;

    /** 计划类型 1=力量 2=有氧 3=混合 */
    private Integer planType;

    /** 描述 */
    private String description;

    /** 预估时长分钟 */
    private Integer estimatedMin;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;

    /** 计划状态 0=DRAFT 1=IN_PROGRESS 2=COMPLETED 3=CANCELLED（V3 追加） */
    private Integer status;

    /** 开始训练时间戳，start 接口写入（V3 追加） */
    private LocalDateTime startedAt;

    /** 完成训练时间戳，complete 接口写入（V3 追加） */
    private LocalDateTime completedAt;

    /** 实际训练时长秒，前端计时器累计提交（V3 追加） */
    private Integer actualDurationSec;
}
