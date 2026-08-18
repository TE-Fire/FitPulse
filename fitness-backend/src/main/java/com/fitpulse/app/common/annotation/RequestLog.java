package com.fitpulse.app.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Controller 请求日志注解（方法级，显式开关）。
 * <p>仅标注了此注解的 Controller 方法才会被 {@code RequestLogAspect} 拦截记录日志，
 * 未标注的方法不产生任何切面开销。
 * <p>用法示例：
 * <pre>
 * &#64;PostMapping("/login")
 * &#64;RequestLog("用户登录")
 * public Result&lt;LoginUserVO&gt; login(@Valid @RequestBody LoginReq req) { ... }
 * </pre>
 *
 * @author FitPulse
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestLog {

    /**
     * 接口业务描述，用于日志头部标识，如"用户登录"、"发送注册验证码"。
     */
    String value() default "";

    /**
     * 是否打印请求入参（含 @RequestBody / @RequestParam / @PathVariable），默认开启。
     */
    boolean logArgs() default true;

    /**
     * 是否打印方法返回值，默认开启。敏感字段会自动脱敏。
     */
    boolean logResult() default true;

    /**
     * 是否打印接口执行耗时（毫秒），默认开启。
     */
    boolean logCost() default true;

    /**
     * 额外需要脱敏的字段名（不区分大小写）。
     * <p>内置默认脱敏字段：password、passwordHash、code、token、accessToken、refreshToken、
     * oldPassword、newPassword、confirmPassword、secret。
     * <p>此处可追加业务字段，如手机号、身份证号等。
     */
    String[] maskFields() default {};
}
