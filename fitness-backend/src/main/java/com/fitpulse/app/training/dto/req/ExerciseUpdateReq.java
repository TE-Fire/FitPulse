package com.fitpulse.app.training.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改动作请求（部分更新语义，全部可选）。
 * <p>对应接口：PUT /api/v1/training/exercises/{id}
 * <p>仅更新请求体中非 null 的字段。
 * <p>仅允许修改自己的自定义动作（is_system=0 AND user_id=当前用户）。
 */
@Data
public class ExerciseUpdateReq {

    /** 动作名称（1-128 字符） */
    @Size(max = 128, message = "动作名称长度不能超过128")
    private String name;

    /** 动作分类 1-8 */
    @Min(value = 1, message = "动作分类值非法")
    @Max(value = 8, message = "动作分类值非法")
    private Integer category;

    /** 难度 1-3 */
    @Min(value = 1, message = "难度值非法")
    @Max(value = 3, message = "难度值非法")
    private Integer difficulty;

    /** 器械（≤64 字符） */
    @Size(max = 64, message = "器械长度不能超过64")
    private String equipment;

    /** 目标肌群（≤128 字符） */
    @Size(max = 128, message = "目标肌群长度不能超过128")
    private String muscleGroup;

    /** 动作说明 */
    private String description;

    /** 示范图 URL（≤512 字符） */
    @Size(max = 512, message = "示范图URL长度不能超过512")
    private String imageUrl;
}
