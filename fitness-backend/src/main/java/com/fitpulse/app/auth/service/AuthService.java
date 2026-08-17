package com.fitpulse.app.auth.service;

import com.fitpulse.app.auth.dto.req.LoginReq;
import com.fitpulse.app.auth.dto.req.RefreshReq;
import com.fitpulse.app.auth.dto.req.RegisterReq;
import com.fitpulse.app.auth.dto.req.RegisterSendCodeReq;
import com.fitpulse.app.auth.dto.req.SendCodeReq;
import com.fitpulse.app.auth.dto.vo.LoginUserVO;
import com.fitpulse.app.auth.dto.vo.SendCodeResp;

/**
 * Auth 业务接口：注册 / 登录 / 发送验证码 / 刷新 / 登出。
 * <p>Controller 面向此接口注入，Spring 自动装配 impl 包下的实现类。
 */
public interface AuthService {

    /**
     * 用户注册（校验注册验证码后创建 user 记录，不自动登录）。
     */
    void register(RegisterReq req);

    /**
     * 发送 6 位注册验证码到 QQ 邮箱（60s 内防刷，key 前缀与登录验证码完全隔离）。
     * <p>返回响应对象中 code 明文与控制台日志、邮件正文、Redis 存储值一致，便于本地/演示联调。
     */
    SendCodeResp registerSendCode(RegisterSendCodeReq req);

    /**
     * 登录：type=1 密码登录 / type=2 验证码登录。
     */
    LoginUserVO login(LoginReq req);

    /**
     * 发送 6 位登录验证码到 QQ 邮箱（60s 内防刷）。
     * <p>返回响应对象中 code 明文与控制台日志、邮件正文、Redis 存储值一致，便于本地/演示联调。
     */
    SendCodeResp sendCode(SendCodeReq req);

    /**
     * 用 refreshToken 换取新的双 Token（旋转失效）。
     */
    LoginUserVO refresh(RefreshReq req);

    /**
     * 登出：删除 Redis 中的 refreshToken（下次 refresh 即失效）。
     */
    void logout(Long userId);
}
