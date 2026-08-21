package com.fitpulse.app.training.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 新建训练计划请求。
 * <p>对应接口：POST /api/v1/training/plans
 * <p>user_id / status 由后端自动填充（user_id=当前用户，status=DRAFT(0)）。
 * <p>exercises 至少 1 个，每个 exerciseId 必须存在。
 */
@Data
public class PlanCreateReq {

    /** 计划名称（1-128 字符） */
    @NotBlank(message = "计划名称不能为空")
    @Size(max = 128, message = "计划名称长度不能超过128")
    private String name;

    /** 计划类型 1=力量 2=有氧 3=混合 */
    @NotNull(message = "计划类型不能为空")
    @Min(value = 1, message = "计划类型值非法")
    @Max(value = 3, message = "计划类型值非法")
    private Integer planType;

    /** 描述（≤512 字符） */
    @Size(max = 512, message = "描述长度不能超过512")
    private String description;

    /** 预估时长分钟（≥1） */
    @Min(value = 1, message = "预估时长必须≥1")
    private Integer estimatedMin;

    /** 关联动作列表（至少 1 个） */
    @NotEmpty(message = "计划至少包含1个动作")
    @Valid
    private List<PlanExerciseReq> exercises;
}
