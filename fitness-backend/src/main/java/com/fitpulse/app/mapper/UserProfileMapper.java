package com.fitpulse.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitpulse.app.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户资料 Mapper（对应 user_profile 表）。
 * <p>与 {@link UserMapper} 同放在 common/mapper 包下，供 user 模块 Service 注入使用。
 */
@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {
}
