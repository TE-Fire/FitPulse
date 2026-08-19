package com.fitpulse.app.user.dto.projection;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 训练统计聚合投影（Mapper 返回的中间类型）。
 * <p>【设计模式：投影模式 Projection】
 * <p>不创建完整的 WorkoutRecord 实体，而是用一个轻量级投影类只接收聚合查询需要的字段。
 * <p>这样避免了为聚合查询创建完整实体（scope 蔓延），也避免了用 Map<String,Object> 的类型不安全。
 */
@Data
public class WorkoutStatsProjection {

    /** 累计训练次数 */
    private Long totalWorkouts;

    /** 累计训练容量 */
    private BigDecimal totalVolume;

    /** 最近一次训练日期 */
    private LocalDate lastWorkoutDate;
}
