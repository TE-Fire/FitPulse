package com.fitpulse.app.training.dto.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 复制计划响应。
 * <p>对应接口：POST /api/v1/training/plans/{id}/copy
 * <p>新计划 status 始终为 DRAFT(0)。
 */
@Data
@Builder
public class PlanCopyResp {

    /** 新计划 ID */
    private Long newPlanId;

    /** 新计划名称（"原名 副本"） */
    private String name;

    /** 1=力量 2=有氧 3=混合 */
    private Integer planType;
    private String planTypeLabel;

    /** 0=草稿 */
    private Integer status;
    private String statusText;

    /** 复制的动作数量 */
    private Integer exerciseCount;
}
