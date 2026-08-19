/**
 * 用户 Mock 数据（个人中心页所需的最小集合）
 * 字段对齐设计契约 §6.7 /user：
 *   - GET /me：当前用户资料 + 目标合并返回
 *   - GET /goal：查看目标（已合并到 /me）
 */

function delay(ms = 280) {
  return new Promise(r => setTimeout(r, ms))
}

/** 当前用户 + 目标合并（GET /user/me） */
export async function getMyProfile() {
  await delay()
  return {
    userId: 1876504321098765432,
    username: 'fitpulse',
    email: 'fitpulse@qq.com',
    nickname: 'FitPulse 用户',
    avatarUrl: '',
    gender: 1,            // 0未知 1男 2女
    birthday: '1998-06-12',
    heightCm: 178,
    bio: '专注训练，科学饮食，持续进步。',
    status: 1,            // 1启用
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
