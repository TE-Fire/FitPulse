package com.fitpulse.app.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Controller 请求日志注解（方法级，显式开关）。
 * <p>仅标注了此注解的 Controller 方法才会被 {@code RequestLogAspect} 拦截记录日志，
 * 未标注的方法不产生任何切面开销。
 * <p>日志格式（单行）：
 * <pre>
 * [2026-08-18 POST] AuthController#login params={"username":"fire_dev","password":"******"} response={"code":200,...}
 * </pre>
 *
 * @author FitPulse
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestLog {

    /**
     * 接口业务描述（可选，附加在日志行首）。
     */
    String value() default "";

    /**
     * 是否打印请求入参，默认开启。
     */
    boolean logArgs() default true;

    /**
     * 是否打印方法返回值，默认开启。敏感字段会自动脱敏。
     */
    boolean logResult() default true;

    /**
     * 额外需要脱敏的字段名（不区分大小写）。
     * <p>内置默认脱敏字段：password、passwordHash、code、token、accessToken、refreshToken、
     * oldPassword、newPassword、confirmPassword、secret、captcha。
     */
    String[] maskFields() default {};
}
