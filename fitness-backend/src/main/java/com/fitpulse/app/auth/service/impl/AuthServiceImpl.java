package com.fitpulse.app.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitpulse.app.auth.dto.req.LoginReq;
import com.fitpulse.app.auth.dto.req.RefreshReq;
import com.fitpulse.app.auth.dto.req.RegisterReq;
import com.fitpulse.app.auth.dto.req.RegisterSendCodeReq;
import com.fitpulse.app.auth.dto.req.SendCodeReq;
import com.fitpulse.app.auth.dto.vo.LoginUserVO;
import com.fitpulse.app.auth.enums.AuthErrorCode;
import com.fitpulse.app.auth.jwt.JwtTokenProvider;
import com.fitpulse.app.auth.service.AuthService;
import com.fitpulse.app.common.constants.RedisKeyConstants;
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
 * AuthService 默认实现。
 * <p>所有公共方法加 @Override，私有辅助方法保留在实现类内部（不暴露到接口）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

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

    @Override
    public void register(RegisterReq req) {
        String email = req.getEmail();
        String code = req.getCode();

        // 0. 校验注册验证码（空/格式/过期/错误四场景分别对应专属枚举）
        verifyRegisterCodeAndConsume(email, code);

        // 1. 邮箱唯一检查
        Long exist = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getEmail, email)
        );
        if (exist != null && exist > 0) {
            throw new BusinessException(AuthErrorCode.EMAIL_ALREADY_REGISTERED);
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
        return baseUsername + java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    // ============================== 登录 ==============================

    @Override
    public LoginUserVO login(LoginReq req) {
        // 1. 校验登录类型
        LoginTypeEnum loginType = LoginTypeEnum.fromCode(req.getType());
        if (loginType == null) {
            throw new BusinessException(AuthErrorCode.INVALID_LOGIN_TYPE);
        }

        // 2. 按 email 找用户，未找到/禁用 统一提示（防枚举攻击）
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, req.getEmail())
        );
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(AuthErrorCode.EMAIL_OR_PASSWORD_ERROR);
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
            throw new BusinessException(AuthErrorCode.PASSWORD_EMPTY);
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(AuthErrorCode.EMAIL_OR_PASSWORD_ERROR);
        }
    }

    private void verifyCodeAndConsume(String email, String code) {
        if (!StringUtils.hasText(code) || !code.matches("^\\d{6}$")) {
            throw new BusinessException(AuthErrorCode.CODE_FORMAT_ERROR);
        }
        String key = RedisKeyConstants.buildLoginCodeKey(email);
        Object stored = redisTemplate.opsForValue().get(key);
        if (stored == null) {
            throw new BusinessException(AuthErrorCode.CODE_EXPIRED);
        }
        if (!code.equals(String.valueOf(stored))) {
            throw new BusinessException(AuthErrorCode.CODE_ERROR);
        }
        redisTemplate.delete(key);
    }

    private void verifyRegisterCodeAndConsume(String email, String code) {
        if (!StringUtils.hasText(code)) {
            throw new BusinessException(AuthErrorCode.REGISTER_CODE_EMPTY);
        }
        if (!code.matches("^\\d{6}$")) {
            throw new BusinessException(AuthErrorCode.REGISTER_CODE_FORMAT_ERROR);
        }
        String key = RedisKeyConstants.buildRegisterCodeKey(email);
        Object stored = redisTemplate.opsForValue().get(key);
        if (stored == null) {
            throw new BusinessException(AuthErrorCode.REGISTER_CODE_EXPIRED);
        }
        if (!code.equals(String.valueOf(stored))) {
            throw new BusinessException(AuthErrorCode.REGISTER_CODE_ERROR);
        }
        redisTemplate.delete(key);
    }

    // ============================== 发送验证码 ==============================

    @Override
    public void registerSendCode(RegisterSendCodeReq req) {
        String email = req.getEmail();

        // 1. 注册场景：若邮箱已注册，拒绝发送（让攻击者无法枚举哪些邮箱已注册？这里返回失败属于安全取舍）
        Long exist = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getEmail, email)
        );
        if (exist != null && exist > 0) {
            throw new BusinessException(AuthErrorCode.EMAIL_ALREADY_REGISTERED);
        }

        // 2. 60 秒防刷（key 前缀与登录防刷完全隔离）
        String rateKey = RedisKeyConstants.buildRegisterCodeKey(email) + ":rate";
        Boolean rateExisted = redisTemplate.hasKey(rateKey);
        if (Boolean.TRUE.equals(rateExisted)) {
            throw new BusinessException(AuthErrorCode.REGISTER_SEND_CODE_TOO_FREQUENT);
        }

        // 3. 生成 6 位验证码（首位非零）
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100_000));

        // 4. 存 Redis：注册验证码独立前缀
        redisTemplate.opsForValue().set(
                RedisKeyConstants.buildRegisterCodeKey(email),
                code,
                CODE_EXPIRE_MIN,
                TimeUnit.MINUTES
        );
        redisTemplate.opsForValue().set(rateKey, "1", CODE_RATE_LIMIT_SEC, TimeUnit.SECONDS);

        // 5. 开发期日志输出
        log.info("[注册验证码] email={}, code={}", email, code);

        // 6. 邮件发送：注册场景专属主题
        String content = String.format(
                "您的 FitPulse 注册验证码是：%s，5 分钟内有效，请勿泄露给他人。",
                code
        );
        mailService.sendMail(email, "FitPulse 注册验证码", content);
    }

    @Override
    public void sendCode(SendCodeReq req) {
        String email = req.getEmail();

        // 1. 60 秒防刷
        String rateKey = RedisKeyConstants.buildLoginCodeKey(email) + ":rate";
        Boolean rateExisted = redisTemplate.hasKey(rateKey);
        if (Boolean.TRUE.equals(rateExisted)) {
            throw new BusinessException(AuthErrorCode.SEND_CODE_TOO_FREQUENT);
        }

        // 2. 生成 6 位验证码（首位非零）
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100_000));

        // 3. 存 Redis
        redisTemplate.opsForValue().set(
                RedisKeyConstants.buildLoginCodeKey(email),
                code,
                CODE_EXPIRE_MIN,
                TimeUnit.MINUTES
        );
        redisTemplate.opsForValue().set(rateKey, "1", CODE_RATE_LIMIT_SEC, TimeUnit.SECONDS);

        // 4. 开发期日志输出
        log.info("[验证码] email={}, code={}", email, code);

        // 5. 邮件发送
        String content = String.format(
                "您的 FitPulse 登录验证码是：%s，5 分钟内有效，请勿泄露给他人。",
                code
        );
        mailService.sendMail(email, "FitPulse 登录验证码", content);
    }

    // ============================== 刷新 Token ==============================

    @Override
    public LoginUserVO refresh(RefreshReq req) {
        String refreshToken = req.getRefreshToken();

        // 1. 解析 refreshToken（签名+过期）
        Claims claims;
        try {
            claims = jwtTokenProvider.parseToken(refreshToken);
        } catch (BusinessException e) {
            throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }
        if (!"refresh".equals(claims.get("type"))) {
            throw new BusinessException(AuthErrorCode.NOT_REFRESH_TOKEN);
        }
        Long userId = Long.valueOf(claims.getSubject());

        // 2. Redis 二次校验
        if (!jwtTokenProvider.isRefreshTokenValid(userId, refreshToken)) {
            throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 3. 校验用户状态
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            jwtTokenProvider.revokeRefreshToken(userId);
            throw new BusinessException(AuthErrorCode.ACCOUNT_DISABLED);
        }

        // 4. 旋转
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

    @Override
    public void logout(Long userId) {
        if (userId == null) {
            return;
        }
        jwtTokenProvider.revokeRefreshToken(userId);
        log.info("[登出] userId={}", userId);
    }
}
