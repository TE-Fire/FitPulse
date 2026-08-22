package com.fitpulse.app.training.service;

import com.fitpulse.app.common.result.PageResult;
import com.fitpulse.app.training.dto.vo.RecordDetailVO;
import com.fitpulse.app.training.dto.vo.RecordListVO;

import java.time.LocalDate;

/**
 * 训练记录业务接口（2 方法，查询为主）。
 * <p>训练记录不支持手动提交，仅在完成训练计划时由系统自动生成。
 *
 * @author FitPulse
 */
public interface WorkoutRecordService {

    /**
     * 训练记录分页查询（按日期倒序）。
     *
     * @param userId    当前登录用户 ID
     * @param pageNum   页码
     * @param pageSize  每页条数
     * @param startDate 起始日期（含，可空）
     * @param endDate   截止日期（含，可空）
     * @return 分页结果
     */
    PageResult<RecordListVO> page(Long userId, Integer pageNum, Integer pageSize,
                                  LocalDate startDate, LocalDate endDate);

    /**
     * 训练记录详情（含组明细）。
     *
     * @param userId 当前登录用户 ID
     * @param id     记录 ID
     * @return 记录详情
     */
    RecordDetailVO detail(Long userId, Long id);
}
