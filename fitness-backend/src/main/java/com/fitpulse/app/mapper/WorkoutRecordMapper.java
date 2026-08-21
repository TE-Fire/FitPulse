package com.fitpulse.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitpulse.app.entity.WorkoutRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 训练记录 Mapper（对应 workout_record 表）。
 */
@Mapper
public interface WorkoutRecordMapper extends BaseMapper<WorkoutRecord> {
}
