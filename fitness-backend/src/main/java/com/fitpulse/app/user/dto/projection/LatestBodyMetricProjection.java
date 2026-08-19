package com.fitpulse.app.user.dto.projection;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 最新身体指标投影（Mapper 返回的中间类型）。
 * <p>只接收 body_metric 表中聚合查询需要的字段，避免创建完整实体。
 */
@Data
public class LatestBodyMetricProjection {

    /** 最新体重 */
    private BigDecimal latestWeight;

    /** 最新体脂率 */
    private BigDecimal latestBodyFat;
}
