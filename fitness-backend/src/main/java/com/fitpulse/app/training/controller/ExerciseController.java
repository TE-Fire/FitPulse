package com.fitpulse.app.training.controller;

import com.fitpulse.app.auth.jwt.CurrentUser;
import com.fitpulse.app.common.annotation.RequestLog;
import com.fitpulse.app.common.result.PageResult;
import com.fitpulse.app.common.result.Result;
import com.fitpulse.app.training.dto.req.ExerciseCreateReq;
import com.fitpulse.app.training.dto.req.ExerciseUpdateReq;
import com.fitpulse.app.training.dto.vo.ExerciseVO;
import com.fitpulse.app.training.service.ExerciseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 动作库管理 Controller（5 端点）。
 * <p>所有接口均需认证（SecurityConfig 中 anyRequest().authenticated()）。
 * <p>查询范围：系统预置动作 + 当前用户自定义动作。
 *
 * @author FitPulse
 */
@RestController
@RequestMapping("/api/v1/training/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    /**
     * 动作库分页查询（系统动作 + 当前用户自定义）。
     */
    @GetMapping
    @RequestLog("动作库分页查询")
    public Result<PageResult<ExerciseVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Integer difficulty) {
        Long userId = CurrentUser.getUserId();
        return Result.success(exerciseService.page(userId, pageNum, pageSize, name, category, difficulty));
    }

    /**
     * 动作详情。
     */
    @GetMapping("/{id}")
    @RequestLog("动作详情")
    public Result<ExerciseVO> detail(@PathVariable Long id) {
        Long userId = CurrentUser.getUserId();
        return Result.success(exerciseService.detail(userId, id));
    }

    /**
     * 新增自定义动作（is_system=0, user_id=当前用户）。
     */
    @PostMapping
    @RequestLog("新增自定义动作")
    public Result<ExerciseVO> create(@Valid @RequestBody ExerciseCreateReq req) {
        Long userId = CurrentUser.getUserId();
        return Result.success(exerciseService.create(userId, req));
    }

    /**
     * 修改动作（仅自己的自定义动作，部分更新语义）。
     */
    @PutMapping("/{id}")
    @RequestLog("修改动作")
    public Result<ExerciseVO> update(@PathVariable Long id,
                                     @Valid @RequestBody ExerciseUpdateReq req) {
        Long userId = CurrentUser.getUserId();
        return Result.success(exerciseService.update(userId, id, req));
    }

    /**
     * 删除动作（系统预置拒绝，非本人拒绝，被引用拒绝）。
     */
    @DeleteMapping("/{id}")
    @RequestLog("删除动作")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = CurrentUser.getUserId();
        exerciseService.delete(userId, id);
        return Result.success();
    }
}
