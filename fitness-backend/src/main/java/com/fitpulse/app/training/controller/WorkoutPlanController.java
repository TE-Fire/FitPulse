package com.fitpulse.app.training.controller;

import com.fitpulse.app.auth.jwt.CurrentUser;
import com.fitpulse.app.common.annotation.RequestLog;
import com.fitpulse.app.common.result.PageResult;
import com.fitpulse.app.common.result.Result;
import com.fitpulse.app.training.dto.req.PlanCompleteReq;
import com.fitpulse.app.training.dto.req.PlanCreateReq;
import com.fitpulse.app.training.dto.req.PlanUpdateReq;
import com.fitpulse.app.training.dto.vo.*;
import com.fitpulse.app.training.service.WorkoutPlanService;
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
 * 训练计划管理 Controller（10 端点）。
 * <p>包含 CRUD（5）+ 状态流转（start/complete/cancel/copy/in-progress）（5）。
 * <p>注意：GET /plans/in-progress 是字面路径，Spring MVC 优先匹配字面路径而非 {id} 变量。
 *
 * @author FitPulse
 */
@RestController
@RequestMapping("/api/v1/training/plans")
@RequiredArgsConstructor
public class WorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;

    // ============================== CRUD ==============================

    /**
     * 训练计划列表（分页）。
     */
    @GetMapping
    @RequestLog("训练计划列表")
    public Result<PageResult<PlanListVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = CurrentUser.getUserId();
        return Result.success(workoutPlanService.page(userId, pageNum, pageSize));
    }

    /**
     * 计划详情（含关联动作列表）。
     */
    @GetMapping("/{id}")
    @RequestLog("计划详情")
    public Result<PlanDetailVO> detail(@PathVariable Long id) {
        Long userId = CurrentUser.getUserId();
        return Result.success(workoutPlanService.detail(userId, id));
    }

    /**
     * 新建计划（含批量动作关联）。
     */
    @PostMapping
    @RequestLog("新建训练计划")
    public Result<PlanDetailVO> create(@Valid @RequestBody PlanCreateReq req) {
        Long userId = CurrentUser.getUserId();
        return Result.success(workoutPlanService.create(userId, req));
    }

    /**
     * 修改计划（仅 DRAFT 状态，exercises 全量替换）。
     */
    @PutMapping("/{id}")
    @RequestLog("修改训练计划")
    public Result<PlanDetailVO> update(@PathVariable Long id,
                                       @Valid @RequestBody PlanUpdateReq req) {
        Long userId = CurrentUser.getUserId();
        return Result.success(workoutPlanService.update(userId, id, req));
    }

    /**
     * 删除计划（级联删除关联动作）。
     */
    @DeleteMapping("/{id}")
    @RequestLog("删除训练计划")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = CurrentUser.getUserId();
        workoutPlanService.delete(userId, id);
        return Result.success();
    }

    // ============================== 状态流转 ==============================

    /**
     * 开始训练（DRAFT/CANCELLED → IN_PROGRESS）。
     */
    @PostMapping("/{id}/start")
    @RequestLog("开始训练")
    public Result<PlanStartResp> start(@PathVariable Long id) {
        Long userId = CurrentUser.getUserId();
        return Result.success(workoutPlanService.start(userId, id));
    }

    /**
     * 完成训练（IN_PROGRESS → COMPLETED，自动生成 record + sets）。
     */
    @PostMapping("/{id}/complete")
    @RequestLog("完成训练")
    public Result<PlanCompleteResp> complete(@PathVariable Long id,
                                             @Valid @RequestBody PlanCompleteReq req) {
        Long userId = CurrentUser.getUserId();
        return Result.success(workoutPlanService.complete(userId, id, req));
    }

    /**
     * 放弃训练（IN_PROGRESS → CANCELLED，不生成 record）。
     */
    @PostMapping("/{id}/cancel")
    @RequestLog("放弃训练")
    public Result<Void> cancel(@PathVariable Long id) {
        Long userId = CurrentUser.getUserId();
        workoutPlanService.cancel(userId, id);
        return Result.success();
    }

    /**
     * 复制计划（深拷贝 plan + exercises，新 status=DRAFT）。
     */
    @PostMapping("/{id}/copy")
    @RequestLog("复制训练计划")
    public Result<PlanCopyResp> copy(@PathVariable Long id) {
        Long userId = CurrentUser.getUserId();
        return Result.success(workoutPlanService.copy(userId, id));
    }

    /**
     * 当前进行中训练（恢复计时器）。
     */
    @GetMapping("/in-progress")
    @RequestLog("当前进行中训练")
    public Result<InProgressVO> inProgress() {
        Long userId = CurrentUser.getUserId();
        return Result.success(workoutPlanService.inProgress(userId));
    }
}
