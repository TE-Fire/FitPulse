package com.fitpulse.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitpulse.app.entity.WorkoutPlan;
import org.apache.ibatis.annotations.Mapper;

/**
 * 训练计划 Mapper（对应 workout_plan 表）。
 */
@Mapper
public interface WorkoutPlanMapper extends BaseMapper<WorkoutPlan> {
}
