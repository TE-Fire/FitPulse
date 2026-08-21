package com.fitpulse.app.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 训练记录实体（对应 workout_record 表）。
 * <p>记录只在完成训练计划时由系统自动生成（complete 接口），不支持手动提交。
 * <p>record_date 是 DATE 类型（LocalDate），不是 DATETIME。
 * <p>total_volume 是 DECIMAL(12,2)，精度高于 workout_set 的 DECIMAL(8,2)。
 *
 * @author FitPulse
 */
@Data
@TableName("workout_record")
public class WorkoutRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 归属用户 ID */
    private Long userId;

    /** 来源计划 ID（可空） */
    private Long planId;

    /** 训练日期（DATE 类型，非 DATETIME） */
    private LocalDate recordDate;

    /** 实际时长秒 */
    private Integer durationSec;

    /** 训练总容量 = Σ(weight × reps)，is_warmup=1 的组不参与 */
    private BigDecimal totalVolume;

    /** 总组数 */
    private Integer totalSets;

    /** 总次数 */
    private Integer totalReps;

    /** 训练备注（VARCHAR 1024） */
    private String note;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
