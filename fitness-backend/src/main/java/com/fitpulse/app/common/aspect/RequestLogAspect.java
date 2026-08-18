package com.fitpulse.app.common.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.fitpulse.app.common.annotation.RequestLog;
import com.fitpulse.app.common.util.LogMaskUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller 请求日志切面（单行高性能版）。
 * <p>仅对标注了 {@link RequestLog} 注解的 Controller 方法生效，记录单行日志：
 * <pre>
 * [2026-08-18 POST] AuthController#login params={...} response={...}
 * </pre>
 *
 * <h3>性能优化</h3>
 * <ul>
 *   <li>单次 log.info / log.warn 调用（一次 IO）</li>
 *   <li>StringBuilder 一次性拼接，避免多次字符串中间量</li>
 *   <li>日期格式仅到天（yyyy-MM-dd），使用静态 DateTimeFormatter 避免重复编译模式</li>
 *   <li>无 Trace-ID 生成（去除 UUID 调用）</li>
 *   <li>异常路径用 warn 级别单独输出，避免与正常流混淆</li>
 * </ul>
 *
 * <h3>采集字段</h3>
 * <ul>
 *   <li>日期（yyyy-MM-dd）</li>
 *   <li>请求类型（HTTP Method：GET/POST/PUT/DELETE 等）</li>
 *   <li>请求方法（Controller#method）</li>
 *   <li>参数信息（@RequestBody + @RequestParam + @PathVariable + GET QueryString，含脱敏）</li>
 *   <li>响应信息（方法返回值，含脱敏）</li>
 * </ul>
 *
 * @author FitPulse
 */
@Aspect
@Component
public class RequestLogAspect {

    private static final Logger log = LoggerFactory.getLogger(RequestLogAspect.class);

    /** 日期格式化器（静态常量，避免重复编译模式）。 */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 环绕通知：拦截所有标注 @RequestLog 的方法。
     */
    @Around("@annotation(requestLog)")
    public Object around(ProceedingJoinPoint joinPoint, RequestLog requestLog) throws Throwable {
        // 解析请求上下文（异步线程或非 HTTP 环境下可能为 null）
        HttpServletRequest request = currentRequest();

        // 预先构建请求部分日志（日期、HTTP 方法、Controller#方法、参数）
        String requestPart = buildRequestPart(joinPoint, requestLog, request);

        // 执行目标方法
        Object result = null;
        Throwable error = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            error = ex;
            throw ex;
        } finally {
            // 单次日志调用输出完整一行
            emitLog(requestLog, requestPart, result, error);
        }
    }

    // ============================================================
    // ============  请求部分构建  ==================================
    // ============================================================

    /**
     * 构建请求部分日志：[yyyy-MM-dd METHOD] [desc] Controller#method params={...}
     */
    private String buildRequestPart(ProceedingJoinPoint joinPoint, RequestLog ann, HttpServletRequest request) {
        StringBuilder sb = new StringBuilder(128);

        // 日期 + HTTP 方法
        String date = LocalDate.now().format(DATE_FORMATTER);
        String httpMethod = request != null ? request.getMethod() : "UNKNOWN";
        sb.append('[').append(date).append(' ').append(httpMethod).append("] ");

        // 业务描述（可选）
        if (StrUtil.isNotBlank(ann.value())) {
            sb.append(ann.value()).append(' ');
        }

        // Controller#方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        sb.append(method.getDeclaringClass().getSimpleName())
          .append('#')
          .append(method.getName());

        // 参数
        if (ann.logArgs()) {
            String paramsJson = buildParamsJson(joinPoint, request, ann);
            sb.append(" params=").append(paramsJson);
        }

        return sb.toString();
    }

    /**
     * 构建参数 JSON（含脱敏）。
     * <p>采集 @RequestBody + @RequestParam + @PathVariable，GET 请求额外合并 QueryString。
     */
    private String buildParamsJson(ProceedingJoinPoint joinPoint, RequestLog ann, HttpServletRequest request) {
        Map<String, Object> paramsMap = new HashMap<>();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        // 1. 解析方法参数
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            Object arg = args[i];

            if (isIgnorableArg(arg)) {
                continue;
            }

            String paramName = param.isNamePresent() ? param.getName() : "arg" + i;

            if (isRequestBodyParam(param)) {
                paramsMap.put("body", arg);
            } else {
                // @RequestParam / @PathVariable / 未标注的简单参数
                paramsMap.put(paramName, arg);
            }
        }

        // 2. GET 请求额外解析 QueryString
        if (request != null && "GET".equalsIgnoreCase(request.getMethod())) {
            String queryString = request.getQueryString();
            if (StrUtil.isNotBlank(queryString)) {
                Map<String, String> queryMap = parseQueryString(queryString);
                for (Map.Entry<String, String> entry : queryMap.entrySet()) {
                    paramsMap.putIfAbsent(entry.getKey(), entry.getValue());
                }
            }
        }

        // 3. 脱敏 + 序列化
        if (paramsMap.isEmpty()) {
            return "{}";
        }
        return LogMaskUtil.mask(paramsMap, ann.maskFields());
    }

    // ============================================================
    // ============  日志输出  =====================================
    // ============================================================

    /**
     * 单次日志调用输出完整一行。
     * <p>成功路径用 info 级别，异常路径用 warn 级别。
     */
    private void emitLog(RequestLog ann, String requestPart, Object result, Throwable error) {
        try {
            StringBuilder sb = new StringBuilder(requestPart.length() + 64);
            sb.append(requestPart);

            if (error != null) {
                // 异常路径
                String exType = error.getClass().getSimpleName();
                String exMsg = StrUtil.isBlank(error.getMessage()) ? "(无消息)" : error.getMessage();
                sb.append(" exception=").append(exType).append(':').append(exMsg);
                log.warn(sb.toString());
            } else {
                // 成功路径
                if (ann.logResult() && result != null) {
                    String resultJson = LogMaskUtil.mask(result, ann.maskFields());
                    sb.append(" response=").append(resultJson);
                } else {
                    sb.append(" response=(未记录)");
                }
                log.info(sb.toString());
            }
        } catch (Exception e) {
            // 日志打印失败不影响主流程
            log.warn("[RequestLog] 日志输出失败 err={}", e.getMessage());
        }
    }

    // ============================================================
    // ============  工具方法  =====================================
    // ============================================================

    /**
     * 获取当前 HTTP 请求对象。
     */
    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getRequest();
    }

    /**
     * 解析 QueryString 为 Map（URL 解码）。
     */
    private Map<String, String> parseQueryString(String queryString) {
        Map<String, String> result = new HashMap<>();
        if (StrUtil.isBlank(queryString)) {
            return result;
        }
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            if (StrUtil.isBlank(pair)) {
                continue;
            }
            int idx = pair.indexOf('=');
            String key;
            String value;
            if (idx < 0) {
                key = pair;
                value = "";
            } else {
                key = pair.substring(0, idx);
                value = pair.substring(idx + 1);
            }
            try {
                key = URLUtil.decode(key);
                value = URLUtil.decode(value);
            } catch (Exception ignored) {
                // 解码失败保留原值
            }
            result.put(key, value);
        }
        return result;
    }

    /**
     * 判断参数是否标注了 @RequestBody。
     */
    private boolean isRequestBodyParam(Parameter param) {
        return param.getAnnotation(org.springframework.web.bind.annotation.RequestBody.class) != null;
    }

    /**
     * 判断参数值是否应跳过日志记录。
     */
    private boolean isIgnorableArg(Object arg) {
        if (arg == null) {
            return false;
        }
        return arg instanceof HttpServletRequest
                || arg instanceof HttpServletResponse
                || arg instanceof HttpSession
                || arg instanceof MultipartFile
                || arg instanceof MultipartFile[];
    }
}
