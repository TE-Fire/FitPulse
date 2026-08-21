package com.fitpulse.app.training.dto.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 开始训练响应。
 * <p>对应接口：POST /api/v1/training/plans/{id}/start
 */
@Data
@Builder
public class PlanStartResp {

    private Long planId;

    /** 1=IN_PROGRESS */
    private Integer status;
    private String statusText;

    /** 开始时间戳 */
    private LocalDateTime startedAt;
}
