package com.fitpulse.app.training.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 完成训练请求。
 * <p>对应接口：POST /api/v1/training/plans/{id}/complete
 * <p>提交前端计时器累计的实际训练时长和每组实际数据，
 * 后端校验≥5min 后自动生成 workout_record + workout_set。
 */
@Data
public class PlanCompleteReq {

    /** 前端累计训练秒数（≥1，后端校验≥300） */
    @NotNull(message = "训练时长不能为空")
    @Min(value = 1, message = "训练时长必须≥1秒")
    private Integer durationSec;

    /** 训练备注（≤1024 字符） */
    @Size(max = 1024, message = "备注长度不能超过1024")
    private String note;

    /** 实际完成的组明细（至少 1 组） */
    @NotEmpty(message = "训练记录至少包含1组")
    @Valid
    private List<ActualSetInput> actualSets;

    /**
     * 实际完成的一组数据（严格对齐 workout_set 表字段）。
     * <p>注意：workout_set 表没有 rest_sec 字段（与 workout_plan_exercise 不同）。
     * <p>is_completed / is_warmup 为 TINYINT（0/1），不是 Boolean。
     */
    @Data
    public static class ActualSetInput {

        /** 动作 ID */
        @NotNull(message = "动作ID不能为空")
        private Long exerciseId;

        /** 第 N 组（1-127，TINYINT 范围） */
        @NotNull(message = "组号不能为空")
        @Min(value = 1, message = "组号必须≥1")
        @Max(value = 127, message = "组号不能超过127")
        private Integer setNo;

        /** 重量 kg（DECIMAL 8,2，自重动作可空） */
        private BigDecimal weightKg;

        /** 完成次数（力竭可空） */
        private Integer reps;

        /** 是否完成 1=完成 0=未完成（默认 1） */
        private Integer isCompleted;

        /** 是否热身组 1=热身 0=非热身（默认 0） */
        private Integer isWarmup;

        /** RPE 评分 1-10 */
        @Min(value = 1, message = "RPE必须≥1")
        @Max(value = 10, message = "RPE不能超过10")
        private Integer rpe;
    }
}
