package com.fitpulse.app.training.controller;

import com.fitpulse.app.common.result.PageResult;
import com.fitpulse.app.common.result.Result;
import com.fitpulse.app.training.entity.Exercise;
import com.fitpulse.app.training.entity.WorkoutPlan;
import com.fitpulse.app.training.entity.WorkoutRecord;
import com.fitpulse.app.training.mapper.ExerciseMapper;
import com.fitpulse.app.training.mapper.WorkoutPlanMapper;
import com.fitpulse.app.training.mapper.WorkoutRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/training")
@RequiredArgsConstructor
public class TrainingController {
    private final ExerciseMapper exerciseMapper;
    private final WorkoutPlanMapper planMapper;
    private final WorkoutRecordMapper recordMapper;

    @GetMapping("/exercises")
    public Result<PageResult<Exercise>> listExercise(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "20") Long pageSize,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String keyword) {
        Page<Exercise> p = exerciseMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Exercise>()
                        .eq(category != null, Exercise::getCategory, category)
                        .eq(difficulty != null, Exercise::getDifficulty, difficulty)
                        .and(keyword != null, w -> w.like(Exercise::getName, keyword).or().like(Exercise::getAlias, keyword))
                        .orderByAsc(Exercise::getSort).orderByDesc(Exercise::getId));
        return Result.success(PageResult.of(p));
    }

    @GetMapping("/exercises/{id}")
    public Result<Exercise> getExercise(@PathVariable Long id) {
        return Result.success(exerciseMapper.selectById(id));
    }

    @PostMapping("/exercises")
    public Result<Void> addExercise(@RequestBody Exercise e) {
        exerciseMapper.insert(e);
        return Result.success();
    }

    @PutMapping("/exercises/{id}")
    public Result<Void> updateExercise(@PathVariable Long id, @RequestBody Exercise e) {
        e.setId(id); exerciseMapper.updateById(e);
        return Result.success();
    }

    @DeleteMapping("/exercises/{id}")
    public Result<Void> deleteExercise(@PathVariable Long id) {
        exerciseMapper.deleteById(id);
        return Result.success();
    }

    @GetMapping("/plans")
    public Result<PageResult<WorkoutPlan>> listPlans(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "20") Long pageSize) {
        Page<WorkoutPlan> p = planMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<WorkoutPlan>().eq(WorkoutPlan::getIsTemplate, 1).orderByDesc(WorkoutPlan::getId));
        return Result.success(PageResult.of(p));
    }

    @PostMapping("/plans")
    public Result<Void> addPlan(@RequestBody WorkoutPlan p) {
        if (p.getIsTemplate() == null) p.setIsTemplate(1);
        planMapper.insert(p);
        return Result.success();
    }

    @PutMapping("/plans/{id}")
    public Result<Void> updatePlan(@PathVariable Long id, @RequestBody WorkoutPlan p) {
        p.setId(id); planMapper.updateById(p);
        return Result.success();
    }

    @PostMapping("/records")
    public Result<Void> saveRecord(@RequestBody WorkoutRecord r) {
        recordMapper.insert(r);
        return Result.success();
    }

    @GetMapping("/records")
    public Result<PageResult<WorkoutRecord>> listRecords(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {
        Long uid = 1L;
        Page<WorkoutRecord> p = recordMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<WorkoutRecord>().eq(WorkoutRecord::getUserId, uid).orderByDesc(WorkoutRecord::getStartTime));
        return Result.success(PageResult.of(p));
    }

    @GetMapping("/records/{id}")
    public Result<WorkoutRecord> getRecord(@PathVariable Long id) {
        return Result.success(recordMapper.selectById(id));
    }
}
