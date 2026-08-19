package com.fitpulse.app.user.dto.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 健康概览 VO（对应接口 GET /user/overview）。
 * <p>聚合 body_metric + meal_record + water_log 三张表的当日数据。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthOverviewVO {

    /** 最新体重（kg，null 表示未录入） */
    private BigDecimal latestWeight;

    /** 最新体脂率（%，null 表示未录入） */
    private BigDecimal latestBodyFat;

    /** 今日摄入热量（kcal） */
    private BigDecimal todayCalories;

    /** 今日饮水量（ml） */
    private Integer todayWaterMl;
}
