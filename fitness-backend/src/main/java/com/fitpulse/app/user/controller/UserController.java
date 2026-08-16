package com.fitpulse.app.user.controller;

import com.fitpulse.app.auth.jwt.CurrentUser;
import com.fitpulse.app.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/profile")
    public Result<Map<String, Object>> getProfile() {
        Long uid = CurrentUser.userId();
        return Result.success(Map.of(
                "userId", (Object) (uid == null ? 1L : uid),
                "username", "admin",
                "nickname", "FitPulse 用户",
                "gender", 1,
                "heightCm", 175,
                "activityLevel", 2
        ));
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody Map<String, Object> body) {
        return Result.success();
    }

    @GetMapping("/goal")
    public Result<Map<String, Object>> getGoal() {
        return Result.success(Map.of(
                "goalType", 1,
                "startWeightKg", 80.0,
                "targetWeightKg", 70.0,
                "deadline", "2026-12-31",
                "dailyCalorie", 1800,
                "proteinGPerKg", 1.6
        ));
    }

    @PutMapping("/goal")
    public Result<Void> updateGoal(@RequestBody Map<String, Object> body) {
        return Result.success();
    }

    @PutMapping("/password")
    public Result<Void> changePwd(@RequestBody Map<String, String> body) {
        return Result.success();
    }
}
