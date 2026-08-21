package com.fitpulse.app.training.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitpulse.app.common.exception.BusinessException;
import com.fitpulse.app.common.result.PageResult;
import com.fitpulse.app.entity.Exercise;
import com.fitpulse.app.entity.WorkoutPlanExercise;
import com.fitpulse.app.entity.WorkoutSet;
import com.fitpulse.app.mapper.ExerciseMapper;
import com.fitpulse.app.mapper.WorkoutPlanExerciseMapper;
import com.fitpulse.app.mapper.WorkoutSetMapper;
import com.fitpulse.app.training.dto.req.ExerciseCreateReq;
import com.fitpulse.app.training.dto.req.ExerciseUpdateReq;
import com.fitpulse.app.training.dto.vo.ExerciseVO;
import com.fitpulse.app.training.enums.TrainingErrorCode;
import com.fitpulse.app.training.service.ExerciseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * ExerciseService 默认实现。
 * <p>编码风格对齐 {@link com.fitpulse.app.user.service.impl.UserServiceImpl}：
 * <ul>
 *   <li>@Slf4j + @Service + @RequiredArgsConstructor</li>
 *   <li>异常用模块专属枚举（TrainingErrorCode）</li>
 *   <li>关键节点 log.info 记录</li>
 * </ul>
 *
 * <p>【设计模式 - 工具表模式】categoryLabel / difficultyLabel 的映射使用静态 Map 常量，
 * 避免每次请求 new 对象，同时将"数字→中文"的映射逻辑集中在一处，便于维护。
 *
 * @author FitPulse
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseServiceImpl implements ExerciseService {

    private final ExerciseMapper exerciseMapper;
    private final WorkoutPlanExerciseMapper workoutPlanExerciseMapper;
    private final WorkoutSetMapper workoutSetMapper;

    /** 动作分类 1-8 → 中文标签 */
    private static final Map<Integer, String> CATEGORY_LABELS = Map.of(
            1, "胸", 2, "背", 3, "肩", 4, "手臂",
            5, "腿", 6, "核心", 7, "有氧", 8, "全身"
    );

    /** 难度 1-3 → 中文标签 */
    private static final Map<Integer, String> DIFFICULTY_LABELS = Map.of(
            1, "入门", 2, "中级", 3, "高级"
    );

    // ============================== 分页查询 ==============================

    @Override
    public PageResult<ExerciseVO> page(Long userId, Integer pageNum, Integer pageSize,
                                       String name, Integer category, Integer difficulty) {
        Page<Exercise> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Exercise> wrapper = new LambdaQueryWrapper<>();
        // 查询条件：系统动作（is_system=1）OR 当前用户的自定义动作
        wrapper.and(w -> w.eq(Exercise::getIsSystem, 1)
                          .or().eq(Exercise::getUserId, userId));
        // 可选筛选
        wrapper.like(StringUtils.hasText(name), Exercise::getName, name);
        wrapper.eq(category != null, Exercise::getCategory, category);
        wrapper.eq(difficulty != null, Exercise::getDifficulty, difficulty);
        wrapper.orderByDesc(Exercise::getIsSystem)  // 系统动作优先
               .orderByAsc(Exercise::getCategory)   // 按分类排序
               .orderByDesc(Exercise::getCreatedAt);

        IPage<Exercise> result = exerciseMapper.selectPage(page, wrapper);
        List<ExerciseVO> voList = result.getRecords().stream()
                .map(e -> toVO(e, userId))
                .toList();
        return PageResult.of(result.getTotal(), pageNum, pageSize, voList);
    }

    // ============================== 详情 ==============================

    @Override
    public ExerciseVO detail(Long userId, Long id) {
        Exercise exercise = exerciseMapper.selectById(id);
        if (exercise == null) {
            throw new BusinessException(TrainingErrorCode.EXERCISE_NOT_FOUND);
        }
        return toVO(exercise, userId);
    }

    // ============================== 新增 ==============================

    @Override
    public ExerciseVO create(Long userId, ExerciseCreateReq req) {
        // 名称去重：同一用户下不允许重名
        checkNameDuplicate(req.getName(), null, userId);

        Exercise exercise = new Exercise();
        exercise.setName(req.getName());
        exercise.setCategory(req.getCategory());
        exercise.setDifficulty(req.getDifficulty());
        exercise.setEquipment(req.getEquipment());
        exercise.setMuscleGroup(req.getMuscleGroup());
        exercise.setDescription(req.getDescription());
        exercise.setImageUrl(req.getImageUrl());
        exercise.setIsSystem(0);  // 自定义动作
        exercise.setUserId(userId);

        exerciseMapper.insert(exercise);
        log.info("[动作库] 用户{}新增自定义动作: id={}, name={}", userId, exercise.getId(), exercise.getName());
        return toVO(exercise, userId);
    }

    // ============================== 修改 ==============================

    @Override
    public ExerciseVO update(Long userId, Long id, ExerciseUpdateReq req) {
        Exercise exercise = exerciseMapper.selectById(id);
        if (exercise == null) {
            throw new BusinessException(TrainingErrorCode.EXERCISE_NOT_FOUND);
        }
        // 权限：仅允许修改自己的自定义动作
        checkOwnership(exercise, userId);

        // 名称变更时检查重名
        if (StringUtils.hasText(req.getName()) && !req.getName().equals(exercise.getName())) {
            checkNameDuplicate(req.getName(), id, userId);
            exercise.setName(req.getName());
        }
        if (req.getCategory() != null) {
            exercise.setCategory(req.getCategory());
        }
        if (req.getDifficulty() != null) {
            exercise.setDifficulty(req.getDifficulty());
        }
        if (req.getEquipment() != null) {
            exercise.setEquipment(req.getEquipment());
        }
        if (req.getMuscleGroup() != null) {
            exercise.setMuscleGroup(req.getMuscleGroup());
        }
        if (req.getDescription() != null) {
            exercise.setDescription(req.getDescription());
        }
        if (req.getImageUrl() != null) {
            exercise.setImageUrl(req.getImageUrl());
        }

        exerciseMapper.updateById(exercise);
        log.info("[动作库] 用户{}修改动作: id={}", userId, id);
        return toVO(exercise, userId);
    }

    // ============================== 删除 ==============================

    @Override
    public void delete(Long userId, Long id) {
        Exercise exercise = exerciseMapper.selectById(id);
        if (exercise == null) {
            throw new BusinessException(TrainingErrorCode.EXERCISE_NOT_FOUND);
        }
        // 系统预置动作不可删除
        if (exercise.getIsSystem() != null && exercise.getIsSystem() == 1) {
            throw new BusinessException(TrainingErrorCode.EXERCISE_SYSTEM_CANNOT_DELETE);
        }
        // 非本人自定义动作不可删除
        checkOwnership(exercise, userId);
        // 被计划或记录引用时不可删除
        Long planRefCount = workoutPlanExerciseMapper.selectCount(
                new LambdaQueryWrapper<WorkoutPlanExercise>().eq(WorkoutPlanExercise::getExerciseId, id));
        if (planRefCount > 0) {
            throw new BusinessException(TrainingErrorCode.EXERCISE_IN_USE);
        }
        Long setRefCount = workoutSetMapper.selectCount(
                new LambdaQueryWrapper<WorkoutSet>().eq(WorkoutSet::getExerciseId, id));
        if (setRefCount > 0) {
            throw new BusinessException(TrainingErrorCode.EXERCISE_IN_USE);
        }

        exerciseMapper.deleteById(id);
        log.info("[动作库] 用户{}删除动作: id={}", userId, id);
    }

    // ============================== 私有辅助 ==============================

    /**
     * 检查动作归属权：仅自定义动作且 user_id=当前用户才允许编辑/删除。
     */
    private void checkOwnership(Exercise exercise, Long userId) {
        if (exercise.getIsSystem() != null && exercise.getIsSystem() == 1) {
            throw new BusinessException(TrainingErrorCode.EXERCISE_NOT_YOURS);
        }
        if (exercise.getUserId() == null || !exercise.getUserId().equals(userId)) {
            throw new BusinessException(TrainingErrorCode.EXERCISE_NOT_YOURS);
        }
    }

    /**
     * 名称去重：同一用户下不允许重名（系统动作不参与去重）。
     *
     * @param name       要检查的名称
     * @param excludeId  排除的 ID（修改时排除自身）
     * @param userId     当前用户 ID
     */
    private void checkNameDuplicate(String name, Long excludeId, Long userId) {
        LambdaQueryWrapper<Exercise> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Exercise::getName, name)
               .eq(Exercise::getUserId, userId)
               .eq(Exercise::getIsSystem, 0);
        if (excludeId != null) {
            wrapper.ne(Exercise::getId, excludeId);
        }
        Long count = exerciseMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(TrainingErrorCode.EXERCISE_NAME_DUPLICATED);
        }
    }

    /**
     * Entity → VO 转换，同时填充 categoryLabel / difficultyLabel / isMine。
     */
    private ExerciseVO toVO(Exercise exercise, Long currentUserId) {
        return ExerciseVO.builder()
                .id(exercise.getId())
                .name(exercise.getName())
                .category(exercise.getCategory())
                .categoryLabel(CATEGORY_LABELS.get(exercise.getCategory()))
                .difficulty(exercise.getDifficulty())
                .difficultyLabel(DIFFICULTY_LABELS.get(exercise.getDifficulty()))
                .equipment(exercise.getEquipment())
                .muscleGroup(exercise.getMuscleGroup())
                .description(exercise.getDescription())
                .imageUrl(exercise.getImageUrl())
                .isSystem(exercise.getIsSystem() != null && exercise.getIsSystem() == 1)
                .isMine(exercise.getUserId() != null && exercise.getUserId().equals(currentUserId))
                .build();
    }
}
