package com.fitpulse.app.training.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增自定义动作请求。
 * <p>对应接口：POST /api/v1/training/exercises
 * <p>is_system / user_id 由后端自动填充（is_system=0, user_id=当前用户），前端不传。
 */
@Data
public class ExerciseCreateReq {

    /** 动作名称（1-128 字符） */
    @NotBlank(message = "动作名称不能为空")
    @Size(max = 128, message = "动作名称长度不能超过128")
    private String name;

    /** 动作分类 1=胸 2=背 3=肩 4=手臂 5=腿 6=核心 7=有氧 8=全身 */
    @NotNull(message = "动作分类不能为空")
    @Min(value = 1, message = "动作分类值非法")
    @Max(value = 8, message = "动作分类值非法")
    private Integer category;

    /** 难度 1=入门 2=中级 3=高级 */
    @NotNull(message = "难度不能为空")
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
