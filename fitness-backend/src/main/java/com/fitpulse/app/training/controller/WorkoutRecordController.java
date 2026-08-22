package com.fitpulse.app.training.controller;

import com.fitpulse.app.auth.jwt.CurrentUser;
import com.fitpulse.app.common.annotation.RequestLog;
import com.fitpulse.app.common.result.PageResult;
import com.fitpulse.app.common.result.Result;
import com.fitpulse.app.training.dto.vo.RecordDetailVO;
import com.fitpulse.app.training.dto.vo.RecordListVO;
import com.fitpulse.app.training.service.WorkoutRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 训练记录 Controller（2 端点，查询接口）。
 * <p>训练记录不支持手动提交，仅在 POST /plans/{id}/complete 时由系统自动生成。
 *
 * @author FitPulse
 */
@RestController
@RequestMapping("/api/v1/training/records")
@RequiredArgsConstructor
public class WorkoutRecordController {

    private final WorkoutRecordService workoutRecordService;

    /**
     * 训练记录分页查询（按日期倒序）。
     * <p>前端传日期参数时需遵循 yyyy-MM-dd 格式。
     */
    @GetMapping
    @RequestLog("训练记录分页查询")
    public Result<PageResult<RecordListVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Long userId = CurrentUser.getUserId();
        return Result.success(workoutRecordService.page(userId, pageNum, pageSize, startDate, endDate));
    }

    /**
     * 训练记录详情（含组明细）。
     */
    @GetMapping("/{id}")
    @RequestLog("训练记录详情")
    public Result<RecordDetailVO> detail(@PathVariable Long id) {
        Long userId = CurrentUser.getUserId();
        return Result.success(workoutRecordService.detail(userId, id));
    }
}
