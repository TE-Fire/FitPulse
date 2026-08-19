package com.fitpulse.app.user.service;

import com.fitpulse.app.user.dto.req.ChangePasswordReq;
import com.fitpulse.app.user.dto.req.UpdateAccountReq;
import com.fitpulse.app.user.dto.req.UpdateProfileReq;
import com.fitpulse.app.user.dto.vo.UserProfileVO;

/**
 * User 业务接口：个人资料 / 账号信息 / 密码。
 * <p>Controller 面向此接口注入，Spring 自动装配 impl 包下的实现类。
 * <p>本次范围仅包含 P3 的 4 个方法（profile GET/PUT、account PUT、password PUT）。
 * 头像上传（P5.1）、训练统计（P5.2）、健康概览（P5.3）将在后续阶段补入。
 */
public interface UserService {

    /**
     * 获取用户完整资料（聚合 user + user_profile 联查）。
     * <p>user_profile 不存在时返回空 profile 对象（不报错，兼容注册后未初始化资料的场景）。
     *
     * @param userId 当前登录用户 ID
     */
    UserProfileVO getProfile(Long userId);

    /**
     * 更新基本资料（仅 user_profile 表字段，部分更新语义）。
     * <p>若 user_profile 不存在则自动创建（INSERT），存在则更新（UPDATE）。
     * <p>仅更新请求体中非 null 的字段。
     *
     * @param userId 当前登录用户 ID
     * @param req    更新请求
     */
    void updateProfile(Long userId, UpdateProfileReq req);

    /**
     * 更新账号信息（仅 user 表的 email / phone 字段）。
     * <p>邮箱变更时检查唯一性；仅更新请求体中非 null 的字段。
     *
     * @param userId 当前登录用户 ID
     * @param req    更新请求
     */
    void updateAccount(Long userId, UpdateAccountReq req);

    /**
     * 修改密码（旧密码 BCrypt 比对通过后更新新密码）。
     * <p>不自动失效 Token，客户端可选择是否重新登录。
     *
     * @param userId 当前登录用户 ID
     * @param req    修改密码请求
     */
    void changePassword(Long userId, ChangePasswordReq req);
}
