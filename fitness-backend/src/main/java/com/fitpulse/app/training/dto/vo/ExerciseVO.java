package com.fitpulse.app.training.dto.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 动作库 VO（列表 / 详情统一结构）。
 * <p>category / difficulty 为数字枚举，前端需映射中文展示。
 * <p>categoryLabel / difficultyLabel 为后端返回的中文标签，前端可直接展示。
 * <p>isMine 由后端根据 user_id 判断，前端用于控制编辑/删除按钮可见性。
 *
 * @author FitPulse
 */
@Data
@Builder
public class ExerciseVO {

    /** 雪花 ID */
    private Long id;

    /** 动作名称 */
    private String name;

    /** 动作分类 1-8 */
    private Integer category;

    /** 分类中文标签（如"胸"） */
    private String categoryLabel;

    /** 难度 1-3 */
    private Integer difficulty;

    /** 难度中文标签（如"中级"） */
    private String difficultyLabel;

    /** 器械 */
    private String equipment;

    /** 目标肌群 */
    private String muscleGroup;

    /** 动作说明 */
    private String description;

    /** 示范图 URL */
    private String imageUrl;

    /** true=系统预置 false=自定义 */
    private Boolean isSystem;

    /** true=当前用户自建 */
    private Boolean isMine;
}
