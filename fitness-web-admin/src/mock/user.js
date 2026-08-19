/**
 * 用户 Mock 数据（个人中心页所需集合）
 * 字段对齐设计契约 §6.7 /user：
 *   - GET /me：当前用户资料 + 目标合并返回
 *   - GET /goal：查看目标（已合并到 /me）
 * 扩展字段（对齐 in_progress.md §2.2 ALTER TABLE 计划）：
 *   - profile.weightKg / bodyFatPct / fitnessLevel / theme
 * 新增端点（对齐 in_progress.md §四 API 设计清单 6/7）：
 *   - GET /user/stats     训练统计概览（累计）
 *   - GET /user/overview  健康概览（今日快照）
 *   - POST /user/avatar   头像上传（mock 返回 trae-api 占位图 URL）
 */

function delay(ms = 280) {
  return new Promise(r => setTimeout(r, ms))
}

/** 当前用户 + 目标 + 扩展字段（GET /user/me） */
export async function getMyProfile() {
  await delay()
  return {
    userId: 1876504321098765432,
    username: 'fitpulse',
    email: 'fitpulse@qq.com',
    phone: '138****8888',
    nickname: 'FitPulse 用户',
    avatarUrl: '',
    gender: 1,                // 0未知 1男 2女
    birthday: '1998-06-12',
    heightCm: 178,
    weightKg: 75.4,           // 缓存最新值（来自 body_metric）
    bodyFatPct: 18.6,        // 缓存最新值
    fitnessLevel: 3,         // 1入门 2进阶 3达人 4专业
    bio: '专注训练，科学饮食，持续进步。',
    theme: 1,                // 1浅色 2深色 3跟随系统
    status: 1,               // 1启用
    lastLoginAt: '2026-08-19 22:10:00',
    createdAt: '2026-07-12 09:30:00',
    goal: {
      goalType: 2,           // 1减脂 2增肌 3塑形 4维持健康 5力量举
      targetWeight: 80.0,
      targetBodyFat: 15.0,
      weeklyWorkouts: 4,
      dailyCalories: 2200,
      dailyWaterMl: 2000,
      startDate: '2026-07-12',
      targetDate: '2026-12-31'
    }
  }
}

/** 占位：修改资料（PUT /user/me） */
export async function updateMyProfile(payload) {
  await delay()
  return { ...payload, updatedAt: '2026-08-19 22:10:00' }
}

/** 占位：修改密码（PUT /user/me/password） */
export async function updateMyPassword() {
  await delay()
  return null
}

/** 占位：修改目标（PUT /user/goal） */
export async function updateMyGoal(payload) {
  await delay()
  return { ...payload, updatedAt: '2026-08-19 22:10:00' }
}

/**
 * 训练统计概览（GET /user/stats）
 * 累计训练次数 / 总容量 / 总组数 / 总次数 / 当前连续 / 最长连续 / 上次训练 / 近 6 月月度汇总
 */
export async function getMyTrainingStats() {
  await delay()
  return {
    totalWorkouts: 128,
    totalVolume: 56820,     // kg 累计
    totalSets: 1240,
    totalReps: 8650,
    streakDays: 5,          // 当前连续
    longestStreak: 21,      // 历史最长
    lastWorkoutAt: '2026-08-18 21:30:00',
    monthlySummary: [
      { month: '2026-03', workouts: 12, volume: 9800 },
      { month: '2026-04', workouts: 15, volume: 11200 },
      { month: '2026-05', workouts: 18, volume: 13400 },
      { month: '2026-06', workouts: 16, volume: 12500 },
      { month: '2026-07', workouts: 19, volume: 14200 },
      { month: '2026-08', workouts: 18, volume: 13800 }
    ]
  }
}

/**
 * 健康概览（GET /user/overview）
 * 最新体重/体脂 + 30天变化 + 今日热量/饮水/蛋白质 + 昨晚睡眠
 */
export async function getMyHealthOverview() {
  await delay()
  return {
    latestWeight: 75.4,
    latestBodyFat: 18.6,
    weightChange30d: -1.4,    // 与 30 天前相比变化（kg，负数=减重）
    bodyFatChange30d: -0.5,  // 与 30 天前相比变化（%）
    caloriesToday: 1850,
    caloriesGoal: 2200,
    waterTodayMl: 1450,
    waterGoalMl: 2000,
    proteinTodayG: 132,
    proteinGoalG: 140,        // 1.6g/kg × 体重 ≈ 121g，向上取整
    sleepHoursLastNight: 7.5
  }
}

/**
 * 头像上传（POST /user/avatar）
 * mock 返回 trae-api 占位图 URL，使 UI 能真实显示头像
 */
export async function uploadAvatar() {
  await delay(500)
  const prompt = encodeURIComponent('个人健身头像，简约扁平风插画，正方形，紫色主调')
  return {
    avatarUrl: `https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=${prompt}&image_size=square_hd`,
    uploadedAt: '2026-08-19 22:10:00'
  }
}
