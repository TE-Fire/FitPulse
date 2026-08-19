/**
 * 看板 Mock 数据（开发期前端独立调试）
 * 字段严格对齐设计契约 §6.2：
 *   - TrainingOverview
 *   - HealthOverview
 * 字段名、含义、维度色归属见 docs/设计契约.md §5
 */

function delay(ms = 280) {
  return new Promise(r => setTimeout(r, ms))
}

/** 最近 N 天日期序列，返回 ['MM-DD', ...]，并附带 yyyy-MM-dd */
function lastDays(n) {
  const out = []
  const today = new Date()
  for (let i = n - 1; i >= 0; i--) {
    const d = new Date(today)
    d.setDate(today.getDate() - i)
    const iso = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    out.push(iso)
  }
  return out
}

/** TrainingOverview（B/C 重点） */
export async function getTrainingOverview() {
  await delay()
  const days = lastDays(7)
  const base = [6200, 7100, 5400, 8200, 7800, 9100, 6800]
  const sets = [16, 18, 14, 22, 20, 24, 18]
  return {
    totalWorkoutsThisWeek: 5,
    totalVolumeThisWeek: 44400,   // B 维度
    totalSetsThisWeek: 114,       // B 维度
    totalRepsThisWeek: 612,        // B 维度
    completionRate7d: 0.71,        // C 维度
    streakDays: 5,                 // C 维度
    totalPlans: 3,                 // 辅助
    weeklyVolumeTrend: days.map((date, i) => ({
      date,
      volume: base[i],
      sets: sets[i]
    }))
  }
}

/** HealthOverview（A/B 重点） */
export async function getHealthOverview() {
  await delay()
  const d30 = lastDays(30)
  // 体重在 75.4 ~ 76.8 之间小幅波动
  const weightTrend30d = d30.map((date, i) => ({
    date,
    value: +(76.8 - i * 0.04 + Math.sin(i) * 0.15).toFixed(1)
  }))
  const d7 = lastDays(7)
  const cal = [1850, 2100, 1980, 2300, 2050, 1880, 2200]
  return {
    latestWeight: 75.4,                 // A 维度
    latestBodyFat: 18.6,                // A 维度
    weightTrend30d,                     // A 维度
    caloriesToday: 1850,                // B 维度（健康）
    waterTodayMl: 1450,                 // B 维度
    waterGoalMl: 2000,                  // B 维度
    proteinTodayG: 132,                 // 辅助
    caloriesLast7d: d7.map((date, i) => ({
      date,
      value: cal[i]
    }))                                 // B 维度
  }
}
