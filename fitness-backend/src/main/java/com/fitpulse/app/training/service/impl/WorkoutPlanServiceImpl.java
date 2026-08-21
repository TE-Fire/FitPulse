package com.fitpulse.app.training.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitpulse.app.common.exception.BusinessException;
import com.fitpulse.app.common.result.PageResult;
import com.fitpulse.app.entity.Exercise;
import com.fitpulse.app.entity.WorkoutPlan;
import com.fitpulse.app.entity.WorkoutPlanExercise;
import com.fitpulse.app.entity.WorkoutRecord;
import com.fitpulse.app.entity.WorkoutSet;
import com.fitpulse.app.mapper.ExerciseMapper;
import com.fitpulse.app.mapper.WorkoutPlanExerciseMapper;
import com.fitpulse.app.mapper.WorkoutPlanMapper;
import com.fitpulse.app.mapper.WorkoutRecordMapper;
import com.fitpulse.app.mapper.WorkoutSetMapper;
import com.fitpulse.app.training.dto.req.PlanCompleteReq;
import com.fitpulse.app.training.dto.req.PlanCompleteReq.ActualSetInput;
import com.fitpulse.app.training.dto.req.PlanCreateReq;
import com.fitpulse.app.training.dto.req.PlanExerciseReq;
import com.fitpulse.app.training.dto.req.PlanUpdateReq;
import com.fitpulse.app.training.dto.vo.*;
import com.fitpulse.app.training.enums.TrainingErrorCode;
import com.fitpulse.app.training.service.WorkoutPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * WorkoutPlanService 默认实现。
 *
 * <p>【状态机】
 * <pre>
 *   DRAFT(0) ──start──→ IN_PROGRESS(1) ──complete──→ COMPLETED(2)
 *      │                     │
 *      │                   cancel
 *      │                     ↓
 *      └──────── CANCELLED(3) ──start（可再次开始）──→ IN_PROGRESS(1)
 * </pre>
 *
 * <p>【complete 事务设计】@Transactional 保证三表原子操作：
 * <ol>
 *   <li>更新 workout_plan：status→COMPLETED, completed_at, actual_duration_sec</li>
 *   <li>插入 workout_record：record_date=当天, 统计字段后端计算</li>
 *   <li>批量插入 workout_set：每组实际数据</li>
 * </ol>
 *
 * <p>【容量计算规则】total_volume = Σ(weight × reps)，其中 is_warmup=1 的组不参与（健身惯例）。
 *
 * <p>【N+1 优化】查询计划详情时，先收集 exerciseId 列表，用 selectBatchIds 一次性查询，
 * 避免 N 次单条查询。
 *
 * @author FitPulse
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkoutPlanServiceImpl implements WorkoutPlanService {

    private final WorkoutPlanMapper workoutPlanMapper;
    private final WorkoutPlanExerciseMapper workoutPlanExerciseMapper;
    private final ExerciseMapper exerciseMapper;
    private final WorkoutRecordMapper workoutRecordMapper;
    private final WorkoutSetMapper workoutSetMapper;

    /** 计划类型 1-3 → 中文标签 */
    private static final Map<Integer, String> PLAN_TYPE_LABELS = Map.of(
            1, "力量", 2, "有氧", 3, "混合"
    );

    /** 计划状态 0-3 → 中文标签 */
    private static final Map<Integer, String> STATUS_TEXTS = Map.of(
            0, "草稿", 1, "进行中", 2, "已完成", 3, "已取消"
    );

    /** 训练时长下限：5 分钟 = 300 秒 */
    private static final int MIN_DURATION_SEC = 300;

    // ============================== 列表 ==============================

    @Override
    public PageResult<PlanListVO> page(Long userId, Integer pageNum, Integer pageSize) {
        Page<WorkoutPlan> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WorkoutPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkoutPlan::getUserId, userId)
               .orderByDesc(WorkoutPlan::getCreatedAt);

        IPage<WorkoutPlan> result = workoutPlanMapper.selectPage(page, wrapper);
        List<PlanListVO> voList = result.getRecords().stream()
                .map(this::toListVO)
                .toList();
        return PageResult.of(result.getTotal(), pageNum, pageSize, voList);
    }

    // ============================== 详情 ==============================

    @Override
    public PlanDetailVO detail(Long userId, Long id) {
        return getPlanDetail(userId, id);
    }

    // ============================== 新建 ==============================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlanDetailVO create(Long userId, PlanCreateReq req) {
        // 名称去重
        checkPlanNameDuplicate(req.getName(), null, userId);

        // 插入计划
        WorkoutPlan plan = new WorkoutPlan();
        plan.setUserId(userId);
        plan.setName(req.getName());
        plan.setPlanType(req.getPlanType());
        plan.setDescription(req.getDescription());
        plan.setEstimatedMin(req.getEstimatedMin());
        plan.setStatus(0); // DRAFT
        workoutPlanMapper.insert(plan);

        // 插入关联动作
        insertPlanExercises(plan.getId(), req.getExercises());

        log.info("[训练计划] 用户{}新建计划: id={}, name={}, exerciseCount={}",
                userId, plan.getId(), plan.getName(), req.getExercises().size());
        return getPlanDetail(userId, plan.getId());
    }

    // ============================== 修改 ==============================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlanDetailVO update(Long userId, Long id, PlanUpdateReq req) {
        WorkoutPlan plan = getPlanOwnedByUser(userId, id);

        // 仅 DRAFT 允许修改
        if (plan.getStatus() != null && plan.getStatus() != 0) {
            throw new BusinessException(TrainingErrorCode.PLAN_NOT_DRAFT);
        }

        // 部分更新
        if (StringUtils.hasText(req.getName())) {
            checkPlanNameDuplicate(req.getName(), id, userId);
            plan.setName(req.getName());
        }
        if (req.getPlanType() != null) {
            plan.setPlanType(req.getPlanType());
        }
        if (req.getDescription() != null) {
            plan.setDescription(req.getDescription());
        }
        if (req.getEstimatedMin() != null) {
            plan.setEstimatedMin(req.getEstimatedMin());
        }
        workoutPlanMapper.updateById(plan);

        // exercises 全量替换（先删后插）
        if (req.getExercises() != null && !req.getExercises().isEmpty()) {
            workoutPlanExerciseMapper.delete(
                    new LambdaQueryWrapper<WorkoutPlanExercise>()
                            .eq(WorkoutPlanExercise::getPlanId, id));
            insertPlanExercises(id, req.getExercises());
        }

        log.info("[训练计划] 用户{}修改计划: id={}", userId, id);
        return getPlanDetail(userId, id);
    }

    // ============================== 删除 ==============================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long id) {
        WorkoutPlan plan = getPlanOwnedByUser(userId, id);

        // 物理删除关联动作（workout_plan_exercise 无逻辑删除字段）
        workoutPlanExerciseMapper.delete(
                new LambdaQueryWrapper<WorkoutPlanExercise>()
                        .eq(WorkoutPlanExercise::getPlanId, id));

        // 逻辑删除计划
        workoutPlanMapper.deleteById(id);
        log.info("[训练计划] 用户{}删除计划: id={}", userId, id);
    }

    // ============================== 开始训练 ==============================

    @Override
    public PlanStartResp start(Long userId, Long id) {
        WorkoutPlan plan = getPlanOwnedByUser(userId, id);

        Integer status = plan.getStatus();
        if (status != null && status == 1) {
            throw new BusinessException(TrainingErrorCode.PLAN_ALREADY_IN_PROGRESS);
        }
        if (status != null && status == 2) {
            throw new BusinessException(TrainingErrorCode.PLAN_ALREADY_COMPLETED);
        }
        // 仅 DRAFT(0) 或 CANCELLED(3) 可以开始

        plan.setStatus(1); // IN_PROGRESS
        plan.setStartedAt(LocalDateTime.now());
        workoutPlanMapper.updateById(plan);

        log.info("[训练计划] 用户{}开始训练: planId={}", userId, id);
        return PlanStartResp.builder()
                .planId(plan.getId())
                .status(1)
                .statusText("进行中")
                .startedAt(plan.getStartedAt())
                .build();
    }

    // ============================== 完成训练 ==============================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlanCompleteResp complete(Long userId, Long id, PlanCompleteReq req) {
        WorkoutPlan plan = getPlanOwnedByUser(userId, id);

        // 状态守卫：必须 IN_PROGRESS
        if (plan.getStatus() == null || plan.getStatus() != 1) {
            throw new BusinessException(TrainingErrorCode.PLAN_NOT_IN_PROGRESS);
        }

        // 时长校验：≥5 分钟
        if (req.getDurationSec() == null || req.getDurationSec() < MIN_DURATION_SEC) {
            throw new BusinessException(TrainingErrorCode.PLAN_DURATION_TOO_SHORT);
        }

        // 组明细校验
        List<ActualSetInput> actualSets = req.getActualSets();
        if (actualSets == null || actualSets.isEmpty()) {
            throw new BusinessException(TrainingErrorCode.RECORD_SET_EMPTY);
        }

        // 校验所有 exerciseId 存在
        List<Long> exerciseIds = actualSets.stream()
                .map(ActualSetInput::getExerciseId)
                .distinct()
                .toList();
        Map<Long, Exercise> exerciseMap = exerciseMapper.selectBatchIds(exerciseIds).stream()
                .collect(Collectors.toMap(Exercise::getId, Function.identity()));
        for (ActualSetInput set : actualSets) {
            if (!exerciseMap.containsKey(set.getExerciseId())) {
                throw new BusinessException(TrainingErrorCode.SET_EXERCISE_NOT_FOUND);
            }
        }

        // 计算统计指标
        BigDecimal totalVolume = BigDecimal.ZERO;
        int totalSets = 0;
        int totalReps = 0;
        for (ActualSetInput set : actualSets) {
            boolean isCompleted = set.getIsCompleted() == null || set.getIsCompleted() == 1;
            boolean isWarmup = set.getIsWarmup() != null && set.getIsWarmup() == 1;

            if (isCompleted) {
                totalSets++;
                if (set.getReps() != null) {
                    totalReps += set.getReps();
                }
                // 容量 = 重量 × 次数，热身组不参与
                if (!isWarmup && set.getWeightKg() != null && set.getReps() != null) {
                    totalVolume = totalVolume.add(
                            set.getWeightKg().multiply(BigDecimal.valueOf(set.getReps())));
                }
            }
        }

        // 1. 更新计划
        plan.setStatus(2); // COMPLETED
        plan.setCompletedAt(LocalDateTime.now());
        plan.setActualDurationSec(req.getDurationSec());
        workoutPlanMapper.updateById(plan);

        // 2. 插入训练记录
        WorkoutRecord record = new WorkoutRecord();
        record.setUserId(userId);
        record.setPlanId(id);
        record.setRecordDate(LocalDate.now());
        record.setDurationSec(req.getDurationSec());
        record.setTotalVolume(totalVolume);
        record.setTotalSets(totalSets);
        record.setTotalReps(totalReps);
        record.setNote(req.getNote());
        workoutRecordMapper.insert(record);

        // 3. 批量插入组明细
        for (ActualSetInput set : actualSets) {
            WorkoutSet ws = new WorkoutSet();
            ws.setRecordId(record.getId());
            ws.setExerciseId(set.getExerciseId());
            ws.setSetNo(set.getSetNo());
            ws.setWeightKg(set.getWeightKg());
            ws.setReps(set.getReps());
            ws.setIsCompleted(set.getIsCompleted() != null ? set.getIsCompleted() : 1);
            ws.setIsWarmup(set.getIsWarmup() != null ? set.getIsWarmup() : 0);
            ws.setRpe(set.getRpe());
            workoutSetMapper.insert(ws);
        }

        log.info("[训练计划] 用户{}完成训练: planId={}, recordId={}, volume={}, sets={}, reps={}",
                userId, id, record.getId(), totalVolume, totalSets, totalReps);

        return PlanCompleteResp.builder()
                .recordId(record.getId())
                .planId(id)
                .recordDate(record.getRecordDate())
                .durationSec(req.getDurationSec())
                .totalVolume(totalVolume)
                .totalSets(totalSets)
                .totalReps(totalReps)
                .build();
    }

    // ============================== 放弃训练 ==============================

    @Override
    public void cancel(Long userId, Long id) {
        WorkoutPlan plan = getPlanOwnedByUser(userId, id);

        if (plan.getStatus() == null || plan.getStatus() != 1) {
            throw new BusinessException(TrainingErrorCode.PLAN_NOT_IN_PROGRESS);
        }

        plan.setStatus(3); // CANCELLED
        workoutPlanMapper.updateById(plan);
        log.info("[训练计划] 用户{}放弃训练: planId={}", userId, id);
    }

    // ============================== 复制计划 ==============================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlanCopyResp copy(Long userId, Long id) {
        WorkoutPlan source = getPlanOwnedByUser(userId, id);

        // 深拷贝计划
        WorkoutPlan newPlan = new WorkoutPlan();
        newPlan.setUserId(userId);
        newPlan.setName(source.getName() + " 副本");
        newPlan.setPlanType(source.getPlanType());
        newPlan.setDescription(source.getDescription());
        newPlan.setEstimatedMin(source.getEstimatedMin());
        newPlan.setStatus(0); // DRAFT
        workoutPlanMapper.insert(newPlan);

        // 深拷贝关联动作
        List<WorkoutPlanExercise> sourceExercises = workoutPlanExerciseMapper.selectList(
                new LambdaQueryWrapper<WorkoutPlanExercise>()
                        .eq(WorkoutPlanExercise::getPlanId, id)
                        .orderByAsc(WorkoutPlanExercise::getSortOrder));

        for (WorkoutPlanExercise src : sourceExercises) {
            WorkoutPlanExercise copy = new WorkoutPlanExercise();
            copy.setPlanId(newPlan.getId());
            copy.setExerciseId(src.getExerciseId());
            copy.setSortOrder(src.getSortOrder());
            copy.setTargetSets(src.getTargetSets());
            copy.setTargetReps(src.getTargetReps());
            copy.setRestSec(src.getRestSec());
            copy.setTargetWeightKg(src.getTargetWeightKg());
            workoutPlanExerciseMapper.insert(copy);
        }

        log.info("[训练计划] 用户{}复制计划: sourceId={}, newPlanId={}, exerciseCount={}",
                userId, id, newPlan.getId(), sourceExercises.size());

        return PlanCopyResp.builder()
                .newPlanId(newPlan.getId())
                .name(newPlan.getName())
                .planType(newPlan.getPlanType())
                .planTypeLabel(PLAN_TYPE_LABELS.get(newPlan.getPlanType()))
                .status(0)
                .statusText("草稿")
                .exerciseCount(sourceExercises.size())
                .build();
    }

    // ============================== 当前进行中训练 ==============================

    @Override
    public InProgressVO inProgress(Long userId) {
        WorkoutPlan plan = workoutPlanMapper.selectOne(
                new LambdaQueryWrapper<WorkoutPlan>()
                        .eq(WorkoutPlan::getUserId, userId)
                        .eq(WorkoutPlan::getStatus, 1)); // IN_PROGRESS

        if (plan == null) {
            return InProgressVO.builder()
                    .hasActivePlan(false)
                    .build();
        }

        // 计算已经过秒数
        int elapsedSec = 0;
        if (plan.getStartedAt() != null) {
            elapsedSec = (int) Duration.between(plan.getStartedAt(), LocalDateTime.now()).getSeconds();
        }

        PlanDetailVO planDetail = getPlanDetail(userId, plan.getId());

        return InProgressVO.builder()
                .hasActivePlan(true)
                .planId(plan.getId())
                .name(plan.getName())
                .startedAt(plan.getStartedAt())
                .elapsedSec(elapsedSec)
                .plan(planDetail)
                .build();
    }

    // ============================== 私有辅助 ==============================

    /**
     * 获取计划并校验归属权。
     */
    private WorkoutPlan getPlanOwnedByUser(Long userId, Long id) {
        WorkoutPlan plan = workoutPlanMapper.selectById(id);
        if (plan == null || !userId.equals(plan.getUserId())) {
            throw new BusinessException(TrainingErrorCode.PLAN_NOT_FOUND);
        }
        return plan;
    }

    /**
     * 名称去重：同一用户下不允许重名计划。
     */
    private void checkPlanNameDuplicate(String name, Long excludeId, Long userId) {
        LambdaQueryWrapper<WorkoutPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkoutPlan::getName, name)
               .eq(WorkoutPlan::getUserId, userId);
        if (excludeId != null) {
            wrapper.ne(WorkoutPlan::getId, excludeId);
        }
        Long count = workoutPlanMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(TrainingErrorCode.PLAN_NAME_DUPLICATED);
        }
    }

    /**
     * 批量插入计划关联动作，校验 exerciseId 存在。
     */
    private void insertPlanExercises(Long planId, List<PlanExerciseReq> exercises) {
        // 校验所有 exerciseId 存在
        List<Long> exerciseIds = exercises.stream()
                .map(PlanExerciseReq::getExerciseId)
                .distinct()
                .toList();
        Map<Long, Exercise> exerciseMap = exerciseMapper.selectBatchIds(exerciseIds).stream()
                .collect(Collectors.toMap(Exercise::getId, Function.identity()));
        for (PlanExerciseReq req : exercises) {
            if (!exerciseMap.containsKey(req.getExerciseId())) {
                throw new BusinessException(TrainingErrorCode.SET_EXERCISE_NOT_FOUND);
            }
        }

        // 逐条插入
        for (PlanExerciseReq req : exercises) {
            WorkoutPlanExercise pe = new WorkoutPlanExercise();
            pe.setPlanId(planId);
            pe.setExerciseId(req.getExerciseId());
            pe.setSortOrder(req.getSortOrder());
            pe.setTargetSets(req.getTargetSets());
            pe.setTargetReps(req.getTargetReps());
            pe.setRestSec(req.getRestSec());
            pe.setTargetWeightKg(req.getTargetWeightKg());
            workoutPlanExerciseMapper.insert(pe);
        }
    }

    /**
     * 获取计划详情（含关联动作列表，N+1 优化：批量查询 exercise.name）。
     */
    private PlanDetailVO getPlanDetail(Long userId, Long planId) {
        WorkoutPlan plan = getPlanOwnedByUser(userId, planId);

        // 查询关联动作
        List<WorkoutPlanExercise> planExercises = workoutPlanExerciseMapper.selectList(
                new LambdaQueryWrapper<WorkoutPlanExercise>()
                        .eq(WorkoutPlanExercise::getPlanId, planId)
                        .orderByAsc(WorkoutPlanExercise::getSortOrder));

        // 批量查询动作名称（避免 N+1）
        List<Long> exerciseIds = planExercises.stream()
                .map(WorkoutPlanExercise::getExerciseId)
                .distinct()
                .toList();
        final Map<Long, String> exerciseNameMap = exerciseIds.isEmpty() ? Map.of() :
                exerciseMapper.selectBatchIds(exerciseIds).stream()
                        .collect(Collectors.toMap(Exercise::getId, Exercise::getName));

        List<PlanExerciseVO> exerciseVOs = planExercises.stream()
                .map(pe -> PlanExerciseVO.builder()
                        .id(pe.getId())
                        .planId(pe.getPlanId())
                        .exerciseId(pe.getExerciseId())
                        .exerciseName(exerciseNameMap.get(pe.getExerciseId()))
                        .sortOrder(pe.getSortOrder())
                        .targetSets(pe.getTargetSets())
                        .targetReps(pe.getTargetReps())
                        .targetWeightKg(pe.getTargetWeightKg())
                        .restSec(pe.getRestSec())
                        .build())
                .toList();

        return PlanDetailVO.builder()
                .id(plan.getId())
                .name(plan.getName())
                .planType(plan.getPlanType())
                .planTypeLabel(PLAN_TYPE_LABELS.get(plan.getPlanType()))
                .status(plan.getStatus())
                .statusText(STATUS_TEXTS.get(plan.getStatus()))
                .description(plan.getDescription())
                .estimatedMin(plan.getEstimatedMin())
                .startedAt(plan.getStartedAt())
                .completedAt(plan.getCompletedAt())
                .actualDurationSec(plan.getActualDurationSec())
                .exercises(exerciseVOs)
                .build();
    }

    /**
     * Entity → PlanListVO，含 exerciseCount 统计。
     */
    private PlanListVO toListVO(WorkoutPlan plan) {
        Long exerciseCount = workoutPlanExerciseMapper.selectCount(
                new LambdaQueryWrapper<WorkoutPlanExercise>()
                        .eq(WorkoutPlanExercise::getPlanId, plan.getId()));

        return PlanListVO.builder()
                .id(plan.getId())
                .name(plan.getName())
                .planType(plan.getPlanType())
                .planTypeLabel(PLAN_TYPE_LABELS.get(plan.getPlanType()))
                .status(plan.getStatus())
                .statusText(STATUS_TEXTS.get(plan.getStatus()))
                .description(plan.getDescription())
                .estimatedMin(plan.getEstimatedMin())
                .exerciseCount(exerciseCount.intValue())
                .startedAt(plan.getStartedAt())
                .completedAt(plan.getCompletedAt())
                .actualDurationSec(plan.getActualDurationSec())
                .createdAt(plan.getCreatedAt())
                .build();
    }
}
