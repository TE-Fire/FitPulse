package com.fitpulse.app.user.dto.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 训练统计概览 VO（对应接口 GET /user/stats）。
 * <p>聚合 workout_record 表的统计数据。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingStatsVO {

    /** 累计训练次数 */
    private Long totalWorkouts;

    /** 累计训练容量（kg） */
    private BigDecimal totalVolume;

    /** 当前连续训练天数 */
    private Integer currentStreak;

    /** 最近一次训练日期（null 表示从未训练） */
    private LocalDate lastWorkoutDate;
}
