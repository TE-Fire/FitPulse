package com.fitpulse.app.training.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitpulse.app.common.exception.BusinessException;
import com.fitpulse.app.common.result.PageResult;
import com.fitpulse.app.entity.Exercise;
import com.fitpulse.app.entity.WorkoutPlan;
import com.fitpulse.app.entity.WorkoutRecord;
import com.fitpulse.app.entity.WorkoutSet;
import com.fitpulse.app.mapper.ExerciseMapper;
import com.fitpulse.app.mapper.WorkoutPlanMapper;
import com.fitpulse.app.mapper.WorkoutRecordMapper;
import com.fitpulse.app.mapper.WorkoutSetMapper;
import com.fitpulse.app.training.dto.vo.RecordDetailVO;
import com.fitpulse.app.training.dto.vo.RecordListVO;
import com.fitpulse.app.training.dto.vo.RecordSetVO;
import com.fitpulse.app.training.enums.TrainingErrorCode;
import com.fitpulse.app.training.service.WorkoutRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * WorkoutRecordService 默认实现。
 * <p>只提供查询接口（列表 + 详情），训练记录在 complete 接口中由系统自动生成。
 *
 * <p>【N+1 优化策略】
 * 列表页通常每页 10 条记录，每个记录可能对应不同的 planId，
 * 因此采用"收集所有 planId → selectBatchIds → Map 映射"的批量查询模式，
 * 单次 DB 调用即可获取所有计划名称。
 *
 * @author FitPulse
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkoutRecordServiceImpl implements WorkoutRecordService {

    private final WorkoutRecordMapper workoutRecordMapper;
    private final WorkoutSetMapper workoutSetMapper;
    private final WorkoutPlanMapper workoutPlanMapper;
    private final ExerciseMapper exerciseMapper;

    // ============================== 分页查询 ==============================

    @Override
    public PageResult<RecordListVO> page(Long userId, Integer pageNum, Integer pageSize,
                                          LocalDate startDate, LocalDate endDate) {
        Page<WorkoutRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WorkoutRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkoutRecord::getUserId, userId)
               .ge(startDate != null, WorkoutRecord::getRecordDate, startDate)
               .le(endDate != null, WorkoutRecord::getRecordDate, endDate)
               .orderByDesc(WorkoutRecord::getRecordDate)
               .orderByDesc(WorkoutRecord::getCreatedAt);

        IPage<WorkoutRecord> result = workoutRecordMapper.selectPage(page, wrapper);

        // 批量联查计划名称（收集所有 planId，一次 selectBatchIds）
        List<Long> planIds = result.getRecords().stream()
                .map(WorkoutRecord::getPlanId)
                .filter(id -> id != null && id != 0L)
                .distinct()
                .toList();
        Map<Long, WorkoutPlan> planMap = planIds.isEmpty() ? Map.of() :
                workoutPlanMapper.selectBatchIds(planIds).stream()
                        .collect(Collectors.toMap(WorkoutPlan::getId, Function.identity()));

        List<RecordListVO> voList = result.getRecords().stream()
                .map(record -> toListVO(record, planMap))
                .toList();
        return PageResult.of(result.getTotal(), pageNum, pageSize, voList);
    }

    // ============================== 详情 ==============================

    @Override
    public RecordDetailVO detail(Long userId, Long id) {
        WorkoutRecord record = workoutRecordMapper.selectById(id);
        if (record == null || !userId.equals(record.getUserId())) {
            throw new BusinessException(TrainingErrorCode.RECORD_NOT_FOUND);
        }

        // 查询组明细（按 setNo 升序）
        List<WorkoutSet> sets = workoutSetMapper.selectList(
                new LambdaQueryWrapper<WorkoutSet>()
                        .eq(WorkoutSet::getRecordId, id)
                        .orderByAsc(WorkoutSet::getExerciseId)
                        .orderByAsc(WorkoutSet::getSetNo));

        // 批量联查动作名称
        List<Long> exerciseIds = sets.stream()
                .map(WorkoutSet::getExerciseId)
                .distinct()
                .toList();
        Map<Long, Exercise> exerciseMap = exerciseIds.isEmpty() ? Map.of() :
                exerciseMapper.selectBatchIds(exerciseIds).stream()
                        .collect(Collectors.toMap(Exercise::getId, Function.identity()));

        // 联查计划名称
        String planName = null;
        if (record.getPlanId() != null && record.getPlanId() != 0L) {
            WorkoutPlan plan = workoutPlanMapper.selectById(record.getPlanId());
            if (plan != null) {
                planName = plan.getName();
            }
        }

        List<RecordSetVO> setVOs = sets.stream()
                .map(ws -> {
                    Exercise ex = exerciseMap.get(ws.getExerciseId());
                    return RecordSetVO.builder()
                            .id(ws.getId())
                            .exerciseId(ws.getExerciseId())
                            .exerciseName(ex != null ? ex.getName() : null)
                            .setNo(ws.getSetNo())
                            .weightKg(ws.getWeightKg())
                            .reps(ws.getReps())
                            // TINYINT 1/0 → Boolean（展示层语义化）
                            .isCompleted(ws.getIsCompleted() != null && ws.getIsCompleted() == 1)
                            .isWarmup(ws.getIsWarmup() != null && ws.getIsWarmup() == 1)
                            .rpe(ws.getRpe())
                            .build();
                })
                .toList();

        return RecordDetailVO.builder()
                .id(record.getId())
                .planId(record.getPlanId())
                .planName(planName)
                .recordDate(record.getRecordDate())
                .durationSec(record.getDurationSec())
                .totalVolume(record.getTotalVolume())
                .totalSets(record.getTotalSets())
                .totalReps(record.getTotalReps())
                .note(record.getNote())
                .createdAt(record.getCreatedAt())
                .sets(setVOs)
                .build();
    }

    // ============================== 私有辅助 ==============================

    /**
     * Entity → RecordListVO（使用预加载的 planMap 避免 N+1）。
     */
    private RecordListVO toListVO(WorkoutRecord record, Map<Long, WorkoutPlan> planMap) {
        String planName = null;
        if (record.getPlanId() != null && record.getPlanId() != 0L) {
            WorkoutPlan plan = planMap.get(record.getPlanId());
            if (plan != null) {
                planName = plan.getName();
            }
        }

        return RecordListVO.builder()
                .id(record.getId())
                .planId(record.getPlanId())
                .planName(planName)
                .recordDate(record.getRecordDate())
                .durationSec(record.getDurationSec())
                .totalVolume(record.getTotalVolume())
                .totalSets(record.getTotalSets())
                .totalReps(record.getTotalReps())
                .note(record.getNote())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
