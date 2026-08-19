/**
 * 用户 Mock 数据（个人中心页所需集合）
 * 响应结构严格对齐后端 VO：
 *   - UserProfileVO：嵌套 profile 对象（后端 getProfile 返回结构）
 *   - TrainingStatsVO：4 字段（totalWorkouts/totalVolume/currentStreak/lastWorkoutDate）
 *   - HealthOverviewVO：4 字段（latestWeight/latestBodyFat/todayCalories/todayWaterMl）
 *   - AvatarUploadVO：仅 avatarUrl
 */

function delay(ms = 280) {
  return new Promise(r => setTimeout(r, ms))
}

/**
 * 当前用户完整资料（GET /user/profile）
 * 后端返回 UserProfileVO：扁平 user 字段 + 嵌套 profile 对象
 */
export async function getMyProfile() {
  await delay()
  return {
    userId: 1876504321098765432,
    username: 'fitpulse',
    email: 'fitpulse@qq.com',
    phone: '13888888888',
    status: 1,
    lastLoginAt: '2026-08-19 22:10:00',
    createdAt: '2026-07-12 09:30:00',
    profile: {
      nickname: 'FitPulse 用户',
      avatarUrl: '',
      gender: 1,               // 0未知 1男 2女
      birthday: '1998-06-12',
      heightCm: 178,
      weightKg: 75.4,          // 缓存最新值（来自 body_metric）
      bodyFatPct: 18.6,        // 缓存最新值
      fitnessLevel: 3,         // 1入门 2进阶 3达人 4专业
      theme: 1,                // 1浅色 2深色 3跟随系统
      bio: '专注训练，科学饮食，持续进步。'
    }
  }
}

/** 占位：修改资料（PUT /user/profile） */
export async function updateMyProfile(payload) {
  await delay()
  return payload
}

/** 占位：修改账号信息（PUT /user/account） */
export async function updateMyAccount(payload) {
  await delay()
  return payload
}

/** 占位：修改密码（PUT /user/password） */
export async function updateMyPassword() {
  await delay()
  return null
}

/**
 * 训练统计概览（GET /user/stats）
 * 后端返回 TrainingStatsVO：4 字段
 */
export async function getMyTrainingStats() {
  await delay()
  return {
    totalWorkouts: 128,
    totalVolume: 56820,       // kg 累计
    currentStreak: 5,         // 当前连续训练天数
    lastWorkoutDate: '2026-08-18'  // 最近训练日期（null=从未训练）
  }
}

/**
 * 健康概览（GET /user/overview）
 * 后端返回 HealthOverviewVO：4 字段
 */
export async function getMyHealthOverview() {
  await delay()
  return {
    latestWeight: 75.4,       // 最新体重 kg
    latestBodyFat: 18.6,      // 最新体脂率 %
    todayCalories: 1850,      // 今日摄入热量 kcal
    todayWaterMl: 1450        // 今日饮水量 ml
  }
}

/**
 * 头像上传（POST /user/avatar）
 * 后端返回 AvatarUploadVO：仅 avatarUrl
 */
export async function uploadAvatar() {
  await delay(500)
  const prompt = encodeURIComponent('个人健身头像，简约扁平风插画，正方形，紫色主调')
  return {
    avatarUrl: `https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=${prompt}&image_size=square_hd`
  }
}
