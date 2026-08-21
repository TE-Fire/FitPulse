package com.fitpulse.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitpulse.app.entity.Exercise;
import org.apache.ibatis.annotations.Mapper;

/**
 * 动作库 Mapper（对应 exercise 表）。
 */
@Mapper
public interface ExerciseMapper extends BaseMapper<Exercise> {
}
