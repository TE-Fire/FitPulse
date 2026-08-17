package com.fitpulse.app.auth.dto.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送验证码成功响应。
 * <p>code 明文返回仅用于本地/演示环境快速联调；生产上线应置为 null 或走邮件/短信通道。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendCodeResp {

    /** 6 位数字验证码（与控制台 log.info 输出、邮件正文、Redis 存储值一致） */
    private String code;

    /** 验证码有效期（分钟） */
    private Long expireMinutes;

    /** 同邮箱发送频率限制（秒），超过即返回 409 防刷 */
    private Long rateLimitSeconds;
}
