package com.fitpulse.app.training.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 修改训练计划请求（部分更新语义，全部可选）。
 * <p>对应接口：PUT /api/v1/training/plans/{id}
 * <p>仅 status=DRAFT(0) 允许修改。
 * <p>exercises 如果传了则全量替换（先删后插），minItems 1。
 * <p>注意：@Size(min=1) 仅在非 null 时生效，不传 exercises 字段则不替换。
 */
@Data
public class PlanUpdateReq {

    /** 计划名称（1-128 字符） */
    @Size(max = 128, message = "计划名称长度不能超过128")
    private String name;

    /** 计划类型 1-3 */
    @Min(value = 1, message = "计划类型值非法")
    @Max(value = 3, message = "计划类型值非法")
    private Integer planType;

    /** 描述（≤512 字符） */
    @Size(max = 512, message = "描述长度不能超过512")
    private String description;

    /** 预估时长分钟（≥1） */
    @Min(value = 1, message = "预估时长必须≥1")
    private Integer estimatedMin;

    /** 关联动作列表（如果传了则全量替换，至少 1 个） */
    @Size(min = 1, message = "计划至少包含1个动作")
    @Valid
    private List<PlanExerciseReq> exercises;
}
