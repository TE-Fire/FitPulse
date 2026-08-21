package com.fitpulse.app.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 动作库实体（对应 exercise 表）。
 * <p>category 为 TINYINT 数字枚举（1=胸 2=背 3=肩 4=手臂 5=腿 6=核心 7=有氧 8=全身），
 * 不是字符串。is_system 区分系统预置与用户自定义动作。
 * <p>V3 追加字段：muscle_group（目标肌群）。
 *
 * @author FitPulse
 */
@Data
@TableName("exercise")
public class Exercise {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 动作名称 */
    private String name;

    /** 动作分类 1=胸 2=背 3=肩 4=手臂 5=腿 6=核心 7=有氧 8=全身 */
    private Integer category;

    /** 难度 1=入门 2=中级 3=高级 */
    private Integer difficulty;

    /** 器械 */
    private String equipment;

    /** 目标肌群，如胸大肌、股四头肌（V3 追加） */
    private String muscleGroup;

    /** 动作说明 */
    private String description;

    /** 示范图 URL */
    private String imageUrl;

    /** 1=系统预置 0=自定义 */
    private Integer isSystem;

    /** 自定义动作的归属用户（系统动作为 null） */
    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
