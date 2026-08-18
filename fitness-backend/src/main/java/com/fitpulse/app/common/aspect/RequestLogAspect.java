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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controller 请求日志切面。
 * <p>仅对标注了 {@link RequestLog} 注解的 Controller 方法生效，记录：
 * <ul>
 *   <li>Trace-ID：单次请求唯一标识（UUID 前 12 位），便于串联同请求多条日志</li>
 *   <li>时间戳、URI、HTTP 方法、Controller#方法名</li>
 *   <li>客户端 IP、User-Agent</li>
 *   <li>请求参数：@RequestBody + @RequestParam + @PathVariable + QueryString</li>
 *   <li>返回值（自动脱敏敏感字段）</li>
 *   <li>执行耗时、状态（SUCCESS / FAILED）</li>
 * </ul>
 *
 * <h3>日志格式</h3>
 * <pre>
 * ========== [RequestLog] 用户登录 ==========
 * Trace-ID  : 9f3a2b1c8d
 * Timestamp : 2026-08-18 22:30:15.456
 * URI       : /api/v1/auth/login
 * Method    : POST
 * Controller: AuthController#login
 * IP        : 192.168.1.10
 * UA        : Mozilla/5.0 (Windows NT 10.0)
 * Params    : {"username":"fire_dev","password":"******","type":1}
 * ---------- 执行中 ----------
 * Result    : {"code":200,"message":"操作成功","data":{...}}
 * Cost      : 56ms
 * Status    : SUCCESS
 * ========== 请求结束 ==========
 * </pre>
 *
 * @author FitPulse
 */
@Aspect
@Component
public class RequestLogAspect {

    private static final Logger log = LoggerFactory.getLogger(RequestLogAspect.class);

    /**
     * 环绕通知：拦截所有标注 @RequestLog 的方法。
     */
    @Around("@annotation(requestLog)")
    public Object around(ProceedingJoinPoint joinPoint, RequestLog requestLog) throws Throwable {
        // 生成单次请求 Trace-ID，便于串联同请求后续日志
        String traceId = generateTraceId();
        long startTime = System.currentTimeMillis();

        // 解析请求上下文（可能在异步线程或非 HTTP 环境下为 null）
        HttpServletRequest request = currentRequest();

        // 打印请求前日志
        printRequestHeader(traceId, requestLog, joinPoint, request);
        if (requestLog.logArgs()) {
            printRequestArgs(traceId, requestLog, joinPoint, request);
        }

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
            long cost = System.currentTimeMillis() - startTime;
            printResponseFooter(traceId, requestLog, result, error, cost);
        }
    }

    // ============================================================
    // ============  请求头日志（开始部分）  ========================
    // ============================================================

    private void printRequestHeader(String traceId, RequestLog ann, ProceedingJoinPoint joinPoint, HttpServletRequest request) {
        String desc = StrUtil.isBlank(ann.value()) ? "未命名接口" : ann.value();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String controllerName = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();
        String fullMethod = controllerName + "#" + methodName;

        String httpMethod = "UNKNOWN";
        String uri = "-";
        String ip = "-";
        String ua = "-";

        if (request != null) {
            httpMethod = request.getMethod();
            uri = request.getRequestURI();
            ip = resolveClientIp(request);
            ua = request.getHeader("User-Agent");
            if (StrUtil.isBlank(ua)) {
                ua = "-";
            }
        }

        log.info("========== [RequestLog] {} ==========", desc);
        log.info("Trace-ID  : {}", traceId);
        log.info("Timestamp : {}", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
        log.info("URI       : {}", uri);
        log.info("Method    : {}", httpMethod);
        log.info("Controller: {}", fullMethod);
        log.info("IP        : {}", ip);
        log.info("UA        : {}", ua);
    }

    // ============================================================
    // ============  请求参数日志  =================================
    // ============================================================

    /**
     * 打印请求参数：
     * <ol>
     *   <li>POST/PUT/DELETE：从方法入参解析 @RequestBody 对象</li>
     *   <li>GET：额外解析 QueryString + @RequestParam</li>
     *   <li>所有方法：解析 @PathVariable</li>
     * </ol>
     */
    private void printRequestArgs(String traceId, RequestLog ann, ProceedingJoinPoint joinPoint, HttpServletRequest request) {
        Map<String, Object> paramsMap = new HashMap<>();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        // 1. 解析方法参数：@RequestBody / @RequestParam / @PathVariable
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            Object arg = args[i];

            // 跳过不可序列化的内置对象
            if (isIgnorableArg(arg)) {
                continue;
            }

            String paramName = param.isNamePresent() ? param.getName() : "arg" + i;

            // 区分参数类型
            if (isRequestBodyParam(param)) {
                // @RequestBody：整体作为 body 对象
                paramsMap.put("body", arg);
            } else if (isRequestParamParam(param) || isPathVariableParam(param)) {
                // @RequestParam / @PathVariable：按名加入
                paramsMap.put(paramName, arg);
            } else {
                // 未标注注解的简单参数，也按名加入
                paramsMap.put(paramName, arg);
            }
        }

        // 2. GET 请求额外解析 QueryString（处理 @RequestParam 简写形式：String username）
        if (request != null && "GET".equalsIgnoreCase(request.getMethod())) {
            String queryString = request.getQueryString();
            if (StrUtil.isNotBlank(queryString)) {
                Map<String, String> queryMap = parseQueryString(queryString);
                for (Map.Entry<String, String> entry : queryMap.entrySet()) {
                    // 方法参数已解析的优先保留，不覆盖
                    paramsMap.putIfAbsent(entry.getKey(), entry.getValue());
                }
            }
        }

        // 3. 脱敏 + 序列化
        String paramsJson;
        if (paramsMap.isEmpty()) {
            paramsJson = "{}";
        } else {
            paramsJson = LogMaskUtil.mask(paramsMap, ann.maskFields());
        }

        log.info("Params    : {}", paramsJson);
        log.info("---------- 执行中 ----------");
    }

    // ============================================================
    // ============  响应尾日志  ===================================
    // ============================================================

    private void printResponseFooter(String traceId, RequestLog ann, Object result, Throwable error, long cost) {
        try {
            if (error != null) {
                // 异常路径
                String exType = error.getClass().getSimpleName();
                String exMsg = StrUtil.isBlank(error.getMessage()) ? "(无消息)" : error.getMessage();
                log.info("Status    : FAILED");
                log.info("Exception : {}", exType);
                log.info("Message   : {}", exMsg);
            } else {
                // 成功路径
                log.info("Status    : SUCCESS");
                if (ann.logResult() && result != null) {
                    String resultJson = LogMaskUtil.mask(result, ann.maskFields());
                    log.info("Result    : {}", resultJson);
                } else {
                    log.info("Result    : (未记录)");
                }
            }
            if (ann.logCost()) {
                log.info("Cost      : {}ms", cost);
            }
            log.info("========== 请求结束 ==========");
        } catch (Exception e) {
            // 尾部日志打印失败不影响主流程
            log.warn("[RequestLog] 尾部日志打印失败 traceId={} err={}", traceId, e.getMessage());
        }
    }

    // ============================================================
    // ============  工具方法  =====================================
    // ============================================================

    /**
     * 获取当前 HTTP 请求对象。
     * <p>非 HTTP 上下文（如异步线程、定时任务）下返回 null。
     */
    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getRequest();
    }

    /**
     * 生成 12 位 Trace-ID。
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    /**
     * 解析客户端真实 IP。
     * <p>依次尝试：X-Forwarded-For → X-Real-IP → Proxy-Client-IP → WL-Proxy-Client-IP → remoteAddr。
     * <p>X-Forwarded-For 多级代理时取第一个非 unknown 的值。
     */
    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理场景：取第一个
        if (StrUtil.isNotBlank(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 解析 QueryString 为 Map（URL 解码）。
     * <p>对于重复 key 取最后一个；不做 URL 解码失败的容错（出错时原样保留）。
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
     * 判断参数是否标注了 @RequestParam。
     */
    private boolean isRequestParamParam(Parameter param) {
        return param.getAnnotation(org.springframework.web.bind.annotation.RequestParam.class) != null;
    }

    /**
     * 判断参数是否标注了 @PathVariable。
     */
    private boolean isPathVariableParam(Parameter param) {
        return param.getAnnotation(org.springframework.web.bind.annotation.PathVariable.class) != null;
    }

    /**
     * 判断参数值是否应跳过日志记录。
     * <p>跳过：HttpServletRequest/Response、HttpSession、MultipartFile 等不可 JSON 序列化的内置对象。
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
