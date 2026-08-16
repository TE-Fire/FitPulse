package com.fitpulse.app.data.remote.dto

object DashboardDTO {
    // 训练看板 - B(训练容量/组数次数)、C(进度趋势) 为重点
    data class TrainingOverview(
        val totalWorkoutsThisWeek: Int,
        val totalVolumeThisWeek: Double,       // B: 训练容量重点
        val totalSetsThisWeek: Int,            // B: 组数重点
        val totalRepsThisWeek: Int,            // B: 次数重点
        val completionRate7d: Double,          // C: 完成率趋势重点
        val streakDays: Int,
        val totalPlans: Int,
        val weeklyVolumeTrend: List<DailyVolume> // C: 周趋势重点
    )

    data class DailyVolume(val date: String, val volume: Double, val sets: Int)

    // 健康看板 - A(体重体脂)、B(饮水/摄入) 为重点
    data class HealthOverview(
        val latestWeight: Double?,             // A: 体重重点
        val latestBodyFat: Double?,            // A: 体脂重点
        val weightTrend30d: List<DailyMetric>, // A: 趋势重点
        val caloriesToday: Int,                // B: 今日摄入热量重点
        val waterTodayMl: Int,                 // B: 今日饮水重点
        val waterGoalMl: Int,
        val proteinTodayG: Double,
        val caloriesLast7d: List<DailyMetric> // B: 摄入趋势重点
    )

    data class DailyMetric(val date: String, val value: Double)
}
