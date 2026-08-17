package com.fitpulse.app.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitpulse.app.auth.dto.req.LoginReq;
import com.fitpulse.app.auth.dto.req.RefreshReq;
import com.fitpulse.app.auth.dto.req.RegisterReq;
import com.fitpulse.app.auth.dto.req.SendCodeReq;
import com.fitpulse.app.auth.dto.vo.LoginUserVO;
import com.fitpulse.app.auth.jwt.JwtTokenProvider;
import com.fitpulse.app.common.constants.RedisKeyConstants;
import com.fitpulse.app.common.enums.ErrorCodeEnum;
import com.fitpulse.app.common.enums.LoginTypeEnum;
import com.fitpulse.app.common.exception.BusinessException;
import com.fitpulse.app.common.mail.MailService;
import com.fitpulse.app.entity.User;
import com.fitpulse.app.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Auth 业务服务：注册 / 登录 / 发送验证码 / 刷新 / 登出。
 * <p>P1 阶段最小化：只依赖 user 表 + Redis + MailService。
 * Profile/Goal 初始化延后到相应模块创建时补。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    /** 验证码 5 分钟有效 */
    private static final long CODE_EXPIRE_MIN = 5;
    /** 验证码 60 秒发送频率限制 */
    private static final long CODE_RATE_LIMIT_SEC = 60;
    /** 注册默认用户名（从 email @ 前缀截取）冲突时的后缀上限 */
    private static final int USERNAME_UNIQUE_TRIES = 100;

    // ============================== 注册 ==============================

    /**
     * 用户注册（仅创建 user 记录，不自动登录）。
     * <p>校验：邮箱必须 @qq.com；邮箱唯一；密码字母+数字≥8位。
     * <p>生成用户名策略：email @ 前缀作为默认 username，冲突时追加 2/3/... 直到唯一。
     */
    public void register(RegisterReq req) {
        String email = req.getEmail();

        // 1. 邮箱唯一检查
        Long exist = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getEmail, email)
        );
        if (exist != null && exist > 0) {
            throw new BusinessException(ErrorCodeEnum.CONFLICT, "邮箱已注册");
        }

        // 2. 生成唯一 username（从 email 前缀）
        String baseUsername = email.substring(0, email.indexOf('@'));
        String username = resolveUniqueUsername(baseUsername);

        // 3. 插入 user
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setEmail(email);
        user.setStatus(1);
        user.setLastLoginAt(null);
        userMapper.insert(user);

        log.info("[注册成功] userId={}, email={}, username={}", user.getId(), email, username);
    }

    /**
     * 从 baseUsername 出发，追加数字直到数据库唯一。
     * 上限 USERNAME_UNIQUE_TRIES 避免极端情况下死循环。
     */
    private String resolveUniqueUsername(String baseUsername) {
        String candidate = baseUsername;
        int suffix = 2;
        while (suffix < USERNAME_UNIQUE_TRIES) {
            Long count = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, candidate)
            );
            if (count == null || count == 0) {
                return candidate;
            }
            candidate = baseUsername + suffix;
            suffix++;
        }
        // 极端情况（前缀撞了 100 次）直接加 UUID 前 8 位兜底
        return baseUsername + java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    // ============================== 登录 ==============================

    /**
     * 登录：type=1 密码登录 / type=2 验证码登录。
     */
    public LoginUserVO login(LoginReq req) {
        // 1. 校验登录类型
        LoginTypeEnum loginType = LoginTypeEnum.fromCode(req.getType());
        if (loginType == null) {
            throw new BusinessException(ErrorCodeEnum.PARAM_ERROR, "登录类型非法");
        }

        // 2. 按 email 找用户，未找到/禁用 统一提示（防枚举攻击）
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, req.getEmail())
        );
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ErrorCodeEnum.UNAUTHORIZED, "邮箱或密码错误");
        }

        // 3. 分支校验
        switch (loginType) {
            case PASSWORD -> verifyPassword(user, req.getPassword());
            case VERIFY_CODE -> verifyCodeAndConsume(req.getEmail(), req.getCode());
        }

        // 4. 更新 lastLoginAt
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 5. 生成双 Token
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        log.info("[登录成功] userId={}, email={}, type={}", user.getId(), req.getEmail(), loginType);

        return LoginUserVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }

    private void verifyPassword(User user, String password) {
        if (!StringUtils.hasText(password)) {
            throw new BusinessException(ErrorCodeEnum.PARAM_ERROR, "密码不能为空");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(ErrorCodeEnum.UNAUTHORIZED, "邮箱或密码错误");
        }
    }

    private void verifyCodeAndConsume(String email, String code) {
        if (!StringUtils.hasText(code) || !code.matches("^\\d{6}$")) {
            throw new BusinessException(ErrorCodeEnum.PARAM_ERROR, "验证码格式不正确");
        }
        String key = RedisKeyConstants.buildLoginCodeKey(email);
        Object stored = redisTemplate.opsForValue().get(key);
        if (stored == null) {
            throw new BusinessException(ErrorCodeEnum.UNAUTHORIZED, "验证码已过期");
        }
        if (!code.equals(String.valueOf(stored))) {
            throw new BusinessException(ErrorCodeEnum.UNAUTHORIZED, "验证码错误");
        }
        // 一次性消费：成功立即删除
        redisTemplate.delete(key);
    }

    // ============================== 发送验证码 ==============================

    /**
     * 发送 6 位登录验证码到 QQ 邮箱。
     * <p>60s 内重复发送会被限流（Redis 原子判断），验证码 5 分钟有效。
     * <p>开发期同时 log.info 输出验证码方便联调。
     */
    public void sendCode(SendCodeReq req) {
        String email = req.getEmail();

        // 1. 60 秒防刷
        String rateKey = RedisKeyConstants.buildLoginCodeKey(email) + ":rate";
        Boolean rateExisted = redisTemplate.hasKey(rateKey);
        if (Boolean.TRUE.equals(rateExisted)) {
            throw new BusinessException(ErrorCodeEnum.CONFLICT, "发送过于频繁，请 60 秒后再试");
        }

        // 2. 生成 6 位验证码（首位非零）
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100_000));

        // 3. 存 Redis（验证码 + 防刷标记）
        redisTemplate.opsForValue().set(
                RedisKeyConstants.buildLoginCodeKey(email),
                code,
                CODE_EXPIRE_MIN,
                TimeUnit.MINUTES
        );
        redisTemplate.opsForValue().set(rateKey, "1", CODE_RATE_LIMIT_SEC, TimeUnit.SECONDS);

        // 4. 开发期日志输出（方便联调）
        log.info("[验证码] email={}, code={}", email, code);

        // 5. 邮件发送（MailService 内部判断是否配置了凭据，无凭据仅日志不抛错）
        String content = String.format(
                "您的 FitPulse 登录验证码是：%s，5 分钟内有效，请勿泄露给他人。",
                code
        );
        mailService.sendMail(email, "FitPulse 登录验证码", content);
    }

    // ============================== 刷新 Token ==============================

    /**
     * 用 refreshToken 换取新的双 Token（旋转失效：旧的 refreshToken 立即作废）。
     */
    public LoginUserVO refresh(RefreshReq req) {
        String refreshToken = req.getRefreshToken();

        // 1. 解析 refreshToken（签名+过期）
        Claims claims;
        try {
            claims = jwtTokenProvider.parseToken(refreshToken);
        } catch (BusinessException e) {
            throw new BusinessException(ErrorCodeEnum.UNAUTHORIZED, "refreshToken 已失效，请重新登录");
        }
        if (!"refresh".equals(claims.get("type"))) {
            throw new BusinessException(ErrorCodeEnum.PARAM_ERROR, "不是有效的 refreshToken");
        }
        Long userId = Long.valueOf(claims.getSubject());

        // 2. Redis 侧二次校验（防止已登出/已旋转的 token 再用）
        if (!jwtTokenProvider.isRefreshTokenValid(userId, refreshToken)) {
            throw new BusinessException(ErrorCodeEnum.UNAUTHORIZED, "refreshToken 已失效，请重新登录");
        }

        // 3. 查用户信息拿 username（顺便校验用户是否被禁用）
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            jwtTokenProvider.revokeRefreshToken(userId);
            throw new BusinessException(ErrorCodeEnum.UNAUTHORIZED, "账号已禁用，请重新登录");
        }

        // 4. 旋转：销毁旧的，生成新的双 Token
        jwtTokenProvider.revokeRefreshToken(userId);
        String newAccess = jwtTokenProvider.generateAccessToken(userId, user.getUsername());
        String newRefresh = jwtTokenProvider.generateRefreshToken(userId);

        log.info("[Token旋转] userId={}", userId);

        return LoginUserVO.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .userId(userId)
                .username(user.getUsername())
                .build();
    }

    // ============================== 登出 ==============================

    /**
     * 登出：仅删除 Redis 中的 refreshToken（下次 refresh 即失效）。
     * <p>accessToken 无状态，过期前仍然有效（短期 24h 可接受，后续可改黑名单机制）。
     */
    public void logout(Long userId) {
        if (userId == null) {
            return;
        }
        jwtTokenProvider.revokeRefreshToken(userId);
        log.info("[登出] userId={}", userId);
    }
}
