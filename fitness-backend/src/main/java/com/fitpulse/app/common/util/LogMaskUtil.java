package com.fitpulse.app.common.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 日志脱敏工具类。
 * <p>用于 Controller 请求日志打印时，对入参/出参中的敏感字段进行脱敏处理，
 * 避免将密码、Token、验证码等敏感信息明文写入日志。
 *
 * <h3>默认脱敏字段</h3>
 * <ul>
 *   <li>密码类：password、passwordHash、oldPassword、newPassword、confirmPassword、secret</li>
 *   <li>Token 类：token、accessToken、refreshToken</li>
 *   <li>验证码类：code、captcha</li>
 * </ul>
 *
 * <h3>脱敏规则</h3>
 * <ul>
 *   <li>字符串值 → 替换为 {@code ******}</li>
 *   <li>非字符串值（数字、布尔等） → 原样保留（避免类型歧义）</li>
 *   <li>嵌套对象 → 递归处理</li>
 * </ul>
 *
 * @author FitPulse
 */
public final class LogMaskUtil {

    /** 默认需要脱敏的字段名（全部小写存储，匹配时忽略大小写）。 */
    private static final Set<String> DEFAULT_MASK_FIELDS = new HashSet<>(Arrays.asList(
            // 密码类
            "password", "passwordhash", "oldpassword", "newpassword", "confirmpassword", "secret",
            // Token 类
            "token", "accesstoken", "refreshtoken",
            // 验证码类
            "code", "captcha"
    ));

    /** 脱敏占位符。 */
    private static final String MASK_PLACEHOLDER = "******";

    private LogMaskUtil() {
        // 工具类禁止实例化
    }

    /**
     * 对 JSON 字符串中的敏感字段进行脱敏。
     * <p>若输入不是合法 JSON 对象，则原样返回（不抛异常，保证日志打印不会因脱敏逻辑失败而被阻塞）。
     *
     * @param jsonStr      原始 JSON 字符串，可为 null
     * @param extraFields  额外需要脱敏的字段名（与默认集合合并），可为 null 或空
     * @return 脱敏后的 JSON 字符串；输入为 null 时返回 null
     */
    public static String mask(String jsonStr, String[] extraFields) {
        if (StrUtil.isBlank(jsonStr)) {
            return jsonStr;
        }
        try {
            if (!JSONUtil.isTypeJSONObject(jsonStr)) {
                // 非 JSON 对象（可能是数组或基础类型）原样返回，避免破坏结构
                return jsonStr;
            }
            JSONObject obj = JSONUtil.parseObj(jsonStr);
            Set<String> maskSet = buildMaskSet(extraFields);
            maskObject(obj, maskSet);
            return obj.toString();
        } catch (Exception e) {
            // 脱敏过程出现任何异常都不要影响日志记录，原样返回
            return jsonStr;
        }
    }

    /**
     * 对任意对象序列化为 JSON 后进行脱敏。
     *
     * @param obj          原始对象，可为 null
     * @param extraFields  额外需要脱敏的字段名
     * @return 脱敏后的 JSON 字符串；输入为 null 时返回 null
     */
    public static String mask(Object obj, String[] extraFields) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String str) {
            // 字符串可能是 JSON 字符串也可能是普通字符串
            return mask(str, extraFields);
        }
        return mask(JSONUtil.toJsonStr(obj), extraFields);
    }

    /**
     * 合并默认脱敏字段与额外字段。
     */
    private static Set<String> buildMaskSet(String[] extraFields) {
        if (extraFields == null || extraFields.length == 0) {
            return DEFAULT_MASK_FIELDS;
        }
        Set<String> merged = new HashSet<>(DEFAULT_MASK_FIELDS);
        for (String field : extraFields) {
            if (StrUtil.isNotBlank(field)) {
                merged.add(field.toLowerCase());
            }
        }
        return merged;
    }

    /**
     * 递归对 JSONObject 中的敏感字段进行脱敏。
     */
    private static void maskObject(JSONObject obj, Set<String> maskSet) {
        for (String key : obj.keySet()) {
            Object value = obj.get(key);
            if (value == null) {
                continue;
            }
            if (isMaskField(key, maskSet)) {
                // 敏感字段：字符串值直接脱敏；非字符串值（数字、布尔）保留原值避免类型歧义
                if (value instanceof String) {
                    obj.set(key, MASK_PLACEHOLDER);
                }
                continue;
            }
            // 嵌套对象递归处理
            if (value instanceof JSONObject nested) {
                maskObject(nested, maskSet);
            }
        }
    }

    /**
     * 判断字段名是否在脱敏集合中（忽略大小写）。
     */
    private static boolean isMaskField(String fieldName, Set<String> maskSet) {
        if (StrUtil.isBlank(fieldName)) {
            return false;
        }
        return maskSet.contains(fieldName.toLowerCase());
    }
}
