package com.fitpulse.app.user.dto.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 头像上传响应 VO（对应接口 POST /user/avatar）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvatarUploadVO {

    /** 头像可访问 URL */
    private String avatarUrl;
}
