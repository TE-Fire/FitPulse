package com.fitpulse.app.user.controller;

import com.fitpulse.app.auth.jwt.CurrentUser;
import com.fitpulse.app.common.annotation.RequestLog;
import com.fitpulse.app.common.result.Result;
import com.fitpulse.app.user.dto.req.ChangePasswordReq;
import com.fitpulse.app.user.dto.req.UpdateAccountReq;
import com.fitpulse.app.user.dto.req.UpdateProfileReq;
import com.fitpulse.app.user.dto.vo.AvatarUploadVO;
import com.fitpulse.app.user.dto.vo.HealthOverviewVO;
import com.fitpulse.app.user.dto.vo.TrainingStatsVO;
import com.fitpulse.app.user.dto.vo.UserProfileVO;
import com.fitpulse.app.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * User 模块入口（PC 端与移动端共用，符合"单账号个人使用"设计）。
 * <p>所有接口均需认证（SecurityConfig 中 anyRequest().authenticated()）。
 * <p>接口清单：资料 GET/PUT、账号 PUT、密码 PUT、头像 POST、训练统计 GET、健康概览 GET。
 */
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取用户完整资料（聚合 user + user_profile）。
     * <p>user_profile 不存在时返回空 profile 对象（兼容注册后未初始化资料的场景）。
     */
    @GetMapping("/profile")
    @RequestLog("获取用户资料")
    public Result<UserProfileVO> getProfile() {
        Long userId = CurrentUser.getUserId();
        return Result.success(userService.getProfile(userId));
    }

    /**
     * 更新基本资料（user_profile 表，部分更新语义）。
     * <p>若 user_profile 不存在则自动创建（INSERT），存在则更新（UPDATE）。
     * <p>仅更新请求体中非 null 的字段。
     */
    @PutMapping("/profile")
    @RequestLog("更新基本资料")
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileReq req) {
        Long userId = CurrentUser.getUserId();
        userService.updateProfile(userId, req);
        return Result.success();
    }

    /**
     * 更新账号信息（邮箱、手机号）。
     * <p>邮箱变更时检查唯一性；仅更新请求体中非 null 的字段。
     */
    @PutMapping("/account")
    @RequestLog("更新账号信息")
    public Result<Void> updateAccount(@Valid @RequestBody UpdateAccountReq req) {
        Long userId = CurrentUser.getUserId();
        userService.updateAccount(userId, req);
        return Result.success();
    }

    /**
     * 修改密码（旧密码 BCrypt 比对通过后更新新密码）。
     * <p>不自动失效 Token，客户端可选择是否重新登录。
     */
    @PutMapping("/password")
    @RequestLog("修改密码")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordReq req) {
        Long userId = CurrentUser.getUserId();
        userService.changePassword(userId, req);
        return Result.success();
    }

    /**
     * 上传头像（multipart/form-data）。
     * <p>内部委托 FileService 完成文件存储，拿到 URL 后回写 user_profile.avatar_url。
     * <p>支持图片格式：jpg/jpeg/png/webp/gif，大小限制由 Spring MVC 配置。
     *
     * @param file 头像图片文件
     */
    @PostMapping("/avatar")
    @RequestLog("上传头像")
    public Result<AvatarUploadVO> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = CurrentUser.getUserId();
        return Result.success(userService.uploadAvatar(userId, file));
    }

    /**
     * 查询训练统计概览（聚合 workout_record 表）。
     * <p>包含累计训练次数、累计训练容量、当前连续训练天数、最近一次训练日期。
     */
    @GetMapping("/stats")
    @RequestLog("训练统计概览")
    public Result<TrainingStatsVO> getTrainingStats() {
        Long userId = CurrentUser.getUserId();
        return Result.success(userService.getTrainingStats(userId));
    }

    /**
     * 查询健康概览（聚合 body_metric + meal_record + water_log 三表当日数据）。
     * <p>包含最新体重/体脂、今日摄入热量、今日饮水量。
     */
    @GetMapping("/overview")
    @RequestLog("健康概览")
    public Result<HealthOverviewVO> getHealthOverview() {
        Long userId = CurrentUser.getUserId();
        return Result.success(userService.getHealthOverview(userId));
    }
}
