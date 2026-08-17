package com.fitpulse.app.auth.controller;

import com.fitpulse.app.auth.dto.req.LoginReq;
import com.fitpulse.app.auth.dto.req.RefreshReq;
import com.fitpulse.app.auth.dto.req.RegisterReq;
import com.fitpulse.app.auth.dto.req.RegisterSendCodeReq;
import com.fitpulse.app.auth.dto.req.SendCodeReq;
import com.fitpulse.app.auth.dto.vo.LoginUserVO;
import com.fitpulse.app.auth.dto.vo.SendCodeResp;
import com.fitpulse.app.auth.jwt.CurrentUser;
import com.fitpulse.app.auth.service.AuthService;
import com.fitpulse.app.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Auth 模块入口。
 * <p>白名单接口（SecurityConfig permitAll）：/register /register/send-code /login /login/send-code /refresh。
 * <p>需认证接口：/logout（任何当前已登录用户都能调用自己的登出）。
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册（开放接口，必须携带正确的注册验证码）。
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterReq req) {
        authService.register(req);
        return Result.success();
    }

    /**
     * 发送注册验证码（开放接口）。
     * <p>60 秒内重复发送会被限流；邮箱已注册时拒绝发送；验证码 key 前缀与登录场景完全隔离。
     * <p>返回体 data.code 与控制台 log.info 输出、邮件正文、Redis 存储值完全一致，便于本地/演示联调。
     */
    @PostMapping("/register/send-code")
    public Result<SendCodeResp> registerSendCode(@Valid @RequestBody RegisterSendCodeReq req) {
        return Result.success(authService.registerSendCode(req));
    }

    /**
     * 登录（开放接口）。
     * <p>type=1 密码登录 / type=2 验证码登录。
     */
    @PostMapping("/login")
    public Result<LoginUserVO> login(@Valid @RequestBody LoginReq req) {
        return Result.success(authService.login(req));
    }

    /**
     * 发送登录验证码（开放接口）。
     * <p>60 秒内重复发送会被限流（Service 内部用 Redis 控制）。
     * <p>返回体 data.code 与控制台 log.info 输出、邮件正文、Redis 存储值完全一致，便于本地/演示联调。
     */
    @PostMapping("/login/send-code")
    public Result<SendCodeResp> sendCode(@Valid @RequestBody SendCodeReq req) {
        return Result.success(authService.sendCode(req));
    }

    /**
     * 刷新双 Token（开放接口，用 refreshToken 换取新的一对 token）。
     * <p>旋转失效：旧的 refreshToken 立即作废。
     */
    @PostMapping("/refresh")
    public Result<LoginUserVO> refresh(@Valid @RequestBody RefreshReq req) {
        return Result.success(authService.refresh(req));
    }

    /**
     * 登出（需认证）。
     * <p>从 SecurityContext 取当前 userId，删除 Redis 中的 refreshToken。
     * <p>未登录时 JwtAuthFilter 未写入 Context，anyRequest().authenticated() 会在 Security 层直接返回 401。
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        Long userId = CurrentUser.getUserId();
        authService.logout(userId);
        return Result.success();
    }
}
