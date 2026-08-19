package com.fitpulse.app.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitpulse.app.entity.User;
import com.fitpulse.app.entity.UserProfile;
import com.fitpulse.app.file.dto.vo.FileUploadVO;
import com.fitpulse.app.file.service.FileService;
import com.fitpulse.app.mapper.UserMapper;
import com.fitpulse.app.mapper.UserProfileMapper;
import com.fitpulse.app.user.dto.projection.LatestBodyMetricProjection;
import com.fitpulse.app.user.dto.projection.WorkoutStatsProjection;
import com.fitpulse.app.user.dto.req.ChangePasswordReq;
import com.fitpulse.app.user.dto.req.UpdateAccountReq;
import com.fitpulse.app.user.dto.req.UpdateProfileReq;
import com.fitpulse.app.user.dto.vo.AvatarUploadVO;
import com.fitpulse.app.user.dto.vo.HealthOverviewVO;
import com.fitpulse.app.user.dto.vo.TrainingStatsVO;
import com.fitpulse.app.user.dto.vo.UserProfileVO;
import com.fitpulse.app.user.enums.UserErrorCode;
import com.fitpulse.app.mapper.UserStatsMapper;
import com.fitpulse.app.user.service.UserService;
import com.fitpulse.app.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * UserService 默认实现。
 * <p>所有公共方法加 @Override，私有辅助方法保留在实现类内部（不暴露到接口）。
 * <p>编码风格对齐 {@link com.fitpulse.app.auth.service.impl.AuthServiceImpl}：
 * <ul>
 *   <li>@Slf4j + @Service + @RequiredArgsConstructor</li>
 *   <li>私有常量用 private static final</li>
 *   <li>关键节点 log.info 记录</li>
 *   <li>异常用模块专属枚举（UserErrorCode）</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;
    private final UserStatsMapper userStatsMapper;

    // ============================== 获取资料 ==============================

    @Override
    public UserProfileVO getProfile(Long userId) {
        // 1. 查询 user 主表
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }

        // 2. 查询 user_profile（不存在时返回空 profile 对象，兼容注册后未初始化资料的场景）
        UserProfile profile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId)
        );

        // 3. 组装 VO
        return buildUserProfileVO(user, profile);
    }

    private UserProfileVO buildUserProfileVO(User user, UserProfile profile) {
        UserProfileVO.Profile profileVO = (profile == null)
                ? UserProfileVO.Profile.builder().build()
                : UserProfileVO.Profile.builder()
                        .nickname(profile.getNickname())
                        .avatarUrl(profile.getAvatarUrl())
                        .gender(profile.getGender())
                        .birthday(profile.getBirthday())
                        .heightCm(profile.getHeightCm())
                        .weightKg(profile.getWeightKg())
                        .bodyFatPct(profile.getBodyFatPct())
                        .fitnessLevel(profile.getFitnessLevel())
                        .theme(profile.getTheme())
                        .bio(profile.getBio())
                        .build();

        return UserProfileVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .profile(profileVO)
                .build();
    }

    // ============================== 更新基本资料 ==============================

    @Override
    public void updateProfile(Long userId, UpdateProfileReq req) {
        // 1. 校验用户存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }

        // 2. 查询现有 profile
        UserProfile profile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId)
        );

        // 3. 不存在则新建，存在则部分更新
        if (profile == null) {
            UserProfile newProfile = new UserProfile();
            newProfile.setUserId(userId);
            applyProfileFields(newProfile, req);
            userProfileMapper.insert(newProfile);
            log.info("[资料创建] userId={}", userId);
        } else {
            applyProfileFields(profile, req);
            userProfileMapper.updateById(profile);
            log.info("[资料更新] userId={}", userId);
        }
    }

    /**
     * 将 UpdateProfileReq 中非 null 的字段应用到 UserProfile 实体（部分更新语义）。
     */
    private void applyProfileFields(UserProfile entity, UpdateProfileReq req) {
        if (req.getNickname() != null) {
            entity.setNickname(req.getNickname());
        }
        if (req.getGender() != null) {
            entity.setGender(req.getGender());
        }
        if (req.getBirthday() != null) {
            entity.setBirthday(req.getBirthday());
        }
        if (req.getHeightCm() != null) {
            entity.setHeightCm(req.getHeightCm());
        }
        if (req.getWeightKg() != null) {
            entity.setWeightKg(req.getWeightKg());
        }
        if (req.getBodyFatPct() != null) {
            entity.setBodyFatPct(req.getBodyFatPct());
        }
        if (req.getFitnessLevel() != null) {
            entity.setFitnessLevel(req.getFitnessLevel());
        }
        if (req.getTheme() != null) {
            entity.setTheme(req.getTheme());
        }
        if (req.getBio() != null) {
            entity.setBio(req.getBio());
        }
    }

    // ============================== 更新账号信息 ==============================

    @Override
    public void updateAccount(Long userId, UpdateAccountReq req) {
        // 1. 校验至少有一个字段需要更新
        if (req.getEmail() == null && req.getPhone() == null) {
            throw new BusinessException(UserErrorCode.NO_FIELDS_TO_UPDATE);
        }

        // 2. 校验用户存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }

        // 3. 邮箱变更时检查唯一性
        if (req.getEmail() != null && !req.getEmail().equals(user.getEmail())) {
            Long emailCount = userMapper.selectCount(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getEmail, req.getEmail())
                            .ne(User::getId, userId)
            );
            if (emailCount != null && emailCount > 0) {
                throw new BusinessException(UserErrorCode.EMAIL_ALREADY_USED);
            }
            user.setEmail(req.getEmail());
        }

        // 4. 手机号变更时检查唯一性
        if (req.getPhone() != null && !req.getPhone().equals(user.getPhone())) {
            Long phoneCount = userMapper.selectCount(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getPhone, req.getPhone())
                            .ne(User::getId, userId)
            );
            if (phoneCount != null && phoneCount > 0) {
                throw new BusinessException(UserErrorCode.PHONE_ALREADY_USED);
            }
            user.setPhone(req.getPhone());
        }

        userMapper.updateById(user);
        log.info("[账号信息更新] userId={}, emailChanged={}, phoneChanged={}",
                userId, req.getEmail() != null, req.getPhone() != null);
    }

    // ============================== 修改密码 ==============================

    @Override
    public void changePassword(Long userId, ChangePasswordReq req) {
        // 1. 两次密码一致性校验
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new BusinessException(UserErrorCode.PASSWORD_CONFIRM_NOT_MATCH);
        }

        // 2. 校验用户存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }

        // 3. 旧密码 BCrypt 比对
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(UserErrorCode.OLD_PASSWORD_ERROR);
        }

        // 4. 更新密码
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userMapper.updateById(user);

        log.info("[密码修改成功] userId={}", userId);
    }

    // ============================== 头像上传 ==============================

    @Override
    public AvatarUploadVO uploadAvatar(Long userId, MultipartFile file) {
        // 1. 校验用户存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }

        // 2. 委托 FileService 上传文件（bucket=avatar，复用通用上传逻辑）
        // 【门面模式】User 模块不直接处理文件存储，委托给 FileService 完成落盘 + 写 file_resource 表
        FileUploadVO uploadVO = fileService.upload(file, "avatar", userId);
        String avatarUrl = uploadVO.getFileUrl();

        // 3. 更新 user_profile.avatar_url（不存在则自动创建）
        UserProfile profile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId)
        );
        if (profile == null) {
            UserProfile newProfile = new UserProfile();
            newProfile.setUserId(userId);
            newProfile.setAvatarUrl(avatarUrl);
            userProfileMapper.insert(newProfile);
        } else {
            profile.setAvatarUrl(avatarUrl);
            userProfileMapper.updateById(profile);
        }

        log.info("[头像上传成功] userId={}, avatarUrl={}", userId, avatarUrl);
        return AvatarUploadVO.builder().avatarUrl(avatarUrl).build();
    }

    // ============================== 训练统计 ==============================

    @Override
    public TrainingStatsVO getTrainingStats(Long userId) {
        // 1. 查询聚合统计（累计次数、累计容量、最近训练日期）
        WorkoutStatsProjection projection = userStatsMapper.selectWorkoutStats(userId);

        // 2. 计算当前连续训练天数（从今天往前推，遇到没训练的日子就中断）
        LocalDate startDate = LocalDate.now().minusDays(29);  // 最近 30 天
        List<LocalDate> recentDates = userStatsMapper.selectRecentWorkoutDates(userId, startDate);
        int currentStreak = calculateCurrentStreak(recentDates);

        return TrainingStatsVO.builder()
                .totalWorkouts(projection.getTotalWorkouts() != null ? projection.getTotalWorkouts() : 0L)
                .totalVolume(projection.getTotalVolume() != null ? projection.getTotalVolume() : BigDecimal.ZERO)
                .currentStreak(currentStreak)
                .lastWorkoutDate(projection.getLastWorkoutDate())
                .build();
    }

    /**
     * 计算当前连续训练天数。
     * <p>【设计技巧】streak 计算放在 Java 层而非 SQL 层：
     * 1. SQL 窗口函数计算 streak 复杂且难调试
     * 2. Java 层逻辑清晰，易于单元测试
     * 3. 最近 30 天数据量小（最多 30 行），全量查出无性能问题
     * <p>规则：从今天往前数，今天有训练记录则算 1，昨天有则 +1，遇到没训练的日子就中断。
     * 如果今天还没训练但昨天训练了，streak 从昨天开始算（兼容"今天还没练"的场景）。
     */
    private int calculateCurrentStreak(List<LocalDate> sortedDates) {
        if (sortedDates == null || sortedDates.isEmpty()) {
            return 0;
        }
        // dates 已按 DESC 排序（从最近到最早）
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // 确定起始日期：今天有训练则从今天开始，否则从昨天开始（兼容今天还没练的场景）
        LocalDate checkDate;
        if (sortedDates.get(0).equals(today)) {
            checkDate = today;
        } else if (sortedDates.get(0).equals(yesterday)) {
            checkDate = yesterday;
        } else {
            // 最近一次训练在昨天之前，streak 已断
            return 0;
        }

        int streak = 0;
        for (LocalDate date : sortedDates) {
            if (date.equals(checkDate)) {
                streak++;
                checkDate = checkDate.minusDays(1);
            } else if (date.isBefore(checkDate)) {
                // 遇到断档，结束计算
                break;
            }
        }
        return streak;
    }

    // ============================== 健康概览 ==============================

    @Override
    public HealthOverviewVO getHealthOverview(Long userId) {
        // 1. 查询最新身体指标（体重 + 体脂）
        LatestBodyMetricProjection bodyMetric = userStatsMapper.selectLatestBodyMetric(userId);

        // 2. 查询今日摄入热量
        BigDecimal todayCalories = userStatsMapper.selectTodayCalories(userId, LocalDate.now());

        // 3. 查询今日饮水量
        Integer todayWaterMl = userStatsMapper.selectTodayWaterMl(userId, LocalDate.now());

        return HealthOverviewVO.builder()
                .latestWeight(bodyMetric != null ? bodyMetric.getLatestWeight() : null)
                .latestBodyFat(bodyMetric != null ? bodyMetric.getLatestBodyFat() : null)
                .todayCalories(todayCalories != null ? todayCalories : BigDecimal.ZERO)
                .todayWaterMl(todayWaterMl != null ? todayWaterMl : 0)
                .build();
    }
}
