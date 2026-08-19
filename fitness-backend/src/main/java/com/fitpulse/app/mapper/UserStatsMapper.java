package com.fitpulse.app.mapper;

import com.fitpulse.app.user.dto.projection.LatestBodyMetricProjection;
import com.fitpulse.app.user.dto.projection.WorkoutStatsProjection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 用户统计聚合 Mapper。
 * <p>【设计模式：投影模式 + CQRS 查询侧】
 * <p>本 Mapper 专为 user 模块的 stats/overview 查询接口服务，跨多张业务表做聚合查询。
 * <p>不创建 training/health 模块的完整实体（避免 scope 蔓延），而是用 @Select 注解写原生 SQL，
 * 结果映射到轻量级投影类（Projection），实现"按需取字段"。
 * <p>这是单体架构中跨模块聚合的常见做法——查询侧独立于命令侧，不需要业务实体的完整定义。
 * <p>【包位置说明】放在 com.fitpulse.app.mapper 而非 user.mapper，
 * 因为 @MapperScan 只扫描 com.fitpulse.app.mapper 包，与 UserMapper/UserProfileMapper 保持一致。
 */
@Mapper
public interface UserStatsMapper {

    /**
     * 查询用户训练统计聚合（累计次数、累计容量、最近训练日期）。
     * <p>SQL 走 idx_user_date 索引，只扫描该用户的记录。
     */
    @Select("SELECT " +
            "  COUNT(1) AS totalWorkouts, " +
            "  COALESCE(SUM(total_volume), 0) AS totalVolume, " +
            "  MAX(record_date) AS lastWorkoutDate " +
            "FROM workout_record " +
            "WHERE user_id = #{userId} AND deleted = 0")
    WorkoutStatsProjection selectWorkoutStats(@Param("userId") Long userId);

    /**
     * 查询用户最近 N 天有训练记录的日期列表（用于计算连续训练天数）。
     * <p>【设计技巧】连续天数计算放在 Java 层而非 SQL 层，因为：
     * 1. SQL 窗口函数计算 streak 复杂且难调试
     * 2. Java 层计算逻辑清晰，易于单元测试
     * 3. 最近 30 天的数据量很小（最多 30 行），全量查出无性能问题
     */
    @Select("SELECT DISTINCT record_date " +
            "FROM workout_record " +
            "WHERE user_id = #{userId} " +
            "  AND deleted = 0 " +
            "  AND record_date >= #{startDate} " +
            "ORDER BY record_date DESC")
    List<LocalDate> selectRecentWorkoutDates(@Param("userId") Long userId,
                                             @Param("startDate") LocalDate startDate);

    /**
     * 查询用户最新一条身体指标（体重 + 体脂）。
     * <p>按 record_date DESC 取最新一条，只 SELECT 需要的两个字段。
     */
    @Select("SELECT weight_kg AS latestWeight, body_fat_pct AS latestBodyFat " +
            "FROM body_metric " +
            "WHERE user_id = #{userId} AND deleted = 0 " +
            "ORDER BY record_date DESC, updated_at DESC " +
            "LIMIT 1")
    LatestBodyMetricProjection selectLatestBodyMetric(@Param("userId") Long userId);

    /**
     * 查询用户某天摄入总热量。
     */
    @Select("SELECT COALESCE(SUM(total_kcal), 0) " +
            "FROM meal_record " +
            "WHERE user_id = #{userId} " +
            "  AND record_date = #{date} " +
            "  AND deleted = 0")
    BigDecimal selectTodayCalories(@Param("userId") Long userId,
                                   @Param("date") LocalDate date);

    /**
     * 查询用户某天饮水总量（ml）。
     */
    @Select("SELECT COALESCE(SUM(amount_ml), 0) " +
            "FROM water_log " +
            "WHERE user_id = #{userId} " +
            "  AND record_date = #{date}")
    Integer selectTodayWaterMl(@Param("userId") Long userId,
                              @Param("date") LocalDate date);
}
