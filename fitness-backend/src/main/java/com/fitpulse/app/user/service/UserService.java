package com.fitpulse.app.user.service;

import com.fitpulse.app.user.dto.req.ChangePasswordReq;
import com.fitpulse.app.user.dto.req.UpdateAccountReq;
import com.fitpulse.app.user.dto.req.UpdateProfileReq;
import com.fitpulse.app.user.dto.vo.AvatarUploadVO;
import com.fitpulse.app.user.dto.vo.HealthOverviewVO;
import com.fitpulse.app.user.dto.vo.TrainingStatsVO;
import com.fitpulse.app.user.dto.vo.UserProfileVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * User 业务接口：个人资料 / 账号信息 / 密码 / 头像 / 统计。
 * <p>Controller 面向此接口注入，Spring 自动装配 impl 包下的实现类。
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

    /**
     * 上传头像（委托 FileService 存储 + 更新 user_profile.avatar_url）。
     * <p>【门面模式】User 模块不直接处理文件存储，而是委托给 FileService 完成落盘，
     * 自己只负责"拿到 URL 后回写 user_profile"。
     *
     * @param userId 当前登录用户 ID
     * @param file   头像图片文件
     * @return 头像上传响应（含可访问 URL）
     */
    AvatarUploadVO uploadAvatar(Long userId, MultipartFile file);

    /**
     * 查询训练统计概览（聚合 workout_record 表）。
     * <p>包含累计训练次数、累计训练容量、当前连续训练天数、最近一次训练日期。
     *
     * @param userId 当前登录用户 ID
     */
    TrainingStatsVO getTrainingStats(Long userId);

    /**
     * 查询健康概览（聚合 body_metric + meal_record + water_log 三表当日数据）。
     * <p>包含最新体重/体脂、今日摄入热量、今日饮水量。
     *
     * @param userId 当前登录用户 ID
     */
    HealthOverviewVO getHealthOverview(Long userId);
}
