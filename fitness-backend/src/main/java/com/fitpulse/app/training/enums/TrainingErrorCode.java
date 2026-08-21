package com.fitpulse.app.training.enums;

import com.fitpulse.app.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Training 模块专属业务错误枚举。
 * <p>涵盖动作库（Exercise）、训练计划（WorkoutPlan）、训练记录（WorkoutRecord）
 * 三个子域的精确业务异常场景。
 * <p>code 字段沿用 HTTP 语义大类码（400/403/404/409），前端可通过 code 做大类判断。
 *
 * @author FitPulse
 */
@Getter
@AllArgsConstructor
public enum TrainingErrorCode implements BaseExceptionInterface {

    // ========== Exercise 动作库 ==========

    /** 动作不存在 */
    EXERCISE_NOT_FOUND(404, "动作不存在"),

    /** 系统预置动作不可删除 */
    EXERCISE_SYSTEM_CANNOT_DELETE(409, "系统预置动作不可删除"),

    /** 不是您的自定义动作，无法编辑/删除 */
    EXERCISE_NOT_YOURS(403, "不是您的自定义动作，无法编辑/删除"),

    /** 动作名称重复 */
    EXERCISE_NAME_DUPLICATED(409, "动作名称重复"),

    /** 动作被计划或记录引用，不可删除 */
    EXERCISE_IN_USE(409, "动作被计划或记录引用，不可删除"),

    // ========== WorkoutPlan 训练计划 ==========

    /** 计划不存在 */
    PLAN_NOT_FOUND(404, "计划不存在"),

    /** 计划名称重复 */
    PLAN_NAME_DUPLICATED(409, "计划名称重复"),

    /** 计划至少包含1个动作 */
    PLAN_EXERCISE_EMPTY(400, "计划至少包含1个动作"),

    /** 计划不处于草稿状态，无法修改 */
    PLAN_NOT_DRAFT(409, "计划不处于草稿状态，无法修改"),

    /** 计划不处于进行中状态 */
    PLAN_NOT_IN_PROGRESS(409, "计划不处于进行中状态"),

    /** 计划已处于进行中，无法重复开始 */
    PLAN_ALREADY_IN_PROGRESS(409, "计划已处于进行中，无法重复开始"),

    /** 计划已完成，请先复制再开始 */
    PLAN_ALREADY_COMPLETED(409, "计划已完成，请先复制再开始"),

    /** 训练时长不足5分钟，不计入记录 */
    PLAN_DURATION_TOO_SHORT(400, "训练时长不足5分钟，不计入记录"),

    // ========== WorkoutRecord 训练记录 ==========

    /** 训练记录不存在 */
    RECORD_NOT_FOUND(404, "训练记录不存在"),

    /** 训练记录至少包含1组 */
    RECORD_SET_EMPTY(400, "训练记录至少包含1组"),

    /** 训练明细中引用的动作不存在 */
    SET_EXERCISE_NOT_FOUND(400, "训练明细中引用的动作不存在");

    private final Integer errorCode;
    private final String errorMessage;

    @Override
    public Integer getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}
