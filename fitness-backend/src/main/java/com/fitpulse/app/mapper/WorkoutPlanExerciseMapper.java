package com.fitpulse.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitpulse.app.entity.WorkoutPlanExercise;
import org.apache.ibatis.annotations.Mapper;

/**
 * 计划动作关联 Mapper（对应 workout_plan_exercise 表）。
 */
@Mapper
public interface WorkoutPlanExerciseMapper extends BaseMapper<WorkoutPlanExercise> {
}
