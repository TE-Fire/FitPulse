package com.fitpulse.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitpulse.app.entity.WorkoutSet;
import org.apache.ibatis.annotations.Mapper;

/**
 * 训练组明细 Mapper（对应 workout_set 表）。
 */
@Mapper
public interface WorkoutSetMapper extends BaseMapper<WorkoutSet> {
}
