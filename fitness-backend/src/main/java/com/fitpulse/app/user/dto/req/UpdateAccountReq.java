package com.fitpulse.app.user.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 更新账号信息请求（仅更新 user 表的 email / phone 字段）。
 * <p>对应接口：PUT /api/v1/user/account
 * <p>部分更新语义，仅更新请求体中非 null 的字段。邮箱变更时会检查唯一性。
 */
@Data
public class UpdateAccountReq {

    /** 新邮箱（必须以 @qq.com 结尾，变更时检查唯一性） */
    @Email(message = "邮箱格式不正确")
    @Pattern(regexp = ".+@qq\\.com$", message = "仅支持 QQ 邮箱")
    private String email;

    /** 新手机号（11 位数字） */
    @Pattern(regexp = "^\\d{11}$", message = "手机号必须为11位数字")
    private String phone;
}
