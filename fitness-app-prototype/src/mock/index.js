// Mock 数据层
// 与接口文档 docs/接口文档.md 中的响应示例 1:1 对齐
// 原型阶段所有 API 调用都返回这里的 mock,不调真实后端

// 模拟网络延迟
export function delay(ms = 400) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

// 统一成功响应包裹 Result<T>
export function ok(data = null, message = '操作成功') {
  return { code: 200, message, data, timestamp: Date.now() }
}

// =============== Auth 模块 mock ===============

// 发送验证码(注册/登录共用同一明文逻辑,实际前缀隔离在 mock 不体现)
export function mockSendCode(email) {
  const code = String(Math.floor(100000 + Math.random() * 900000))
  return ok({ code, expireMinutes: 5, rateLimitSeconds: 60 })
}

// 登录
export function mockLogin({ email, type, password, code }) {
  // 原型不做严格校验,任意 QQ 邮箱均可登录
  if (!email || !email.endsWith('@qq.com')) {
    return { code: 400, message: '必须为合法的 @qq.com 邮箱', data: null, timestamp: Date.now() }
  }
  if (type === 1 && !password) {
    return { code: 400, message: '密码登录缺少 password', data: null, timestamp: Date.now() }
  }
  if (type === 2 && !code) {
    return { code: 400, message: '验证码登录缺少 code', data: null, timestamp: Date.now() }
  }
  const username = email.split('@')[0]
  return ok({
    accessToken: 'mock-access-token.' + btoa(email),
    refreshToken: 'mock-refresh-token.' + btoa(email),
    userId: 1001,
    username
  })
}

// 注册
export function mockRegister({ email, password, code }) {
  if (!email || !email.endsWith('@qq.com')) {
    return { code: 400, message: '必须为合法的 @qq.com 邮箱', data: null, timestamp: Date.now() }
  }
  if (!password || !code) {
    return { code: 400, message: '密码与验证码不能为空', data: null, timestamp: Date.now() }
  }
  return ok(null, '注册成功')
}

// =============== Dashboard 模块 mock ===============

// 训练看板(对齐接口文档 3.1 响应示例)
export function mockTrainingDashboard() {
  return ok({
    totalWorkoutsThisWeek: 4,
    totalVolumeThisWeek: 28450.00,
    totalSetsThisWeek: 96,
    totalRepsThisWeek: 864,
    completionRate7d: 85.7,
    streakDays: 6,
    totalPlans: 3,
    weeklyVolumeTrend: [
      { date: '08-10', volume: 0.00, sets: 0 },
      { date: '08-11', volume: 6820.00, sets: 22 },
      { date: '08-12', volume: 7180.00, sets: 26 },
      { date: '08-13', volume: 0.00, sets: 0 },
      { date: '08-14', volume: 7350.00, sets: 24 },
      { date: '08-15', volume: 7100.00, sets: 24 },
      { date: '08-16', volume: 0.00, sets: 0 }
    ]
  })
}

// 健康看板(对齐接口文档 3.2 响应示例)
export function mockHealthDashboard() {
  return ok({
    latestWeight: 68.5,
    latestBodyFat: 16.2,
    weightTrend30d: [
      { date: '07-18', value: 70.8 },
      { date: '07-20', value: 70.4 },
      { date: '07-25', value: 69.9 },
      { date: '07-30', value: 69.3 },
      { date: '08-05', value: 68.9 },
      { date: '08-10', value: 68.7 },
      { date: '08-16', value: 68.5 }
    ],
    caloriesToday: 1628.5,
    waterTodayMl: 1400,
    waterGoalMl: 2000,
    proteinTodayG: 96.2,
    caloriesLast7d: [
      { date: '08-10', value: 1720 },
      { date: '08-11', value: 1680 },
      { date: '08-12', value: 1810 },
      { date: '08-13', value: 1550 },
      { date: '08-14', value: 1740 },
      { date: '08-15', value: 1890 },
      { date: '08-16', value: 1628.5 }
    ]
  })
}

// =============== AI 模块 mock ===============

const aiReplies = [
  '根据你今天的训练容量 7100kg,建议明天安排休息日或主动恢复(快走 30min + 拉伸 15min),让胸/三头充分恢复后再继续推下一节课。\n\n营养方面:今日蛋白质摄入 96g 已经达标,睡前可补充 200ml 牛奶补充酪蛋白。',
  '你本周已完成 4 次训练,连续 6 天打卡,完成率 85.7%,状态非常好。\n\n建议下周计划:\n- 周一 推(胸+三头)\n- 周二 拉(背+二头)\n- 周三 休息/有氧 30min\n- 周四 腿日\n- 周五 全身 HIIT\n- 周末 主动恢复',
  '体重 68.5kg,30 天内下降 2.3kg,体脂 16.2% 处于健康区间。如果目标是减脂到 65kg,建议:\n1. 每日热量缺口保持 300-500 kcal\n2. 蛋白质摄入提升到 1.6g/kg 体重 ≈ 110g/天\n3. 每周训练 4-5 次,力量+有氧结合',
  '膝盖疼建议:\n- 立即停止腿弯举等剪切力大的动作\n- 改用保加利亚分腿蹲、臀桥代替\n- 训练后冰敷 10 分钟\n- 若 48 小时未缓解,建议就医检查髌腱'
]

let aiReplyIndex = 0
export function mockAiChat({ message, conversationId }) {
  const reply = aiReplies[aiReplyIndex % aiReplies.length]
  aiReplyIndex++
  return ok({
    reply,
    conversationId: conversationId || 'mock-conv-' + Date.now()
  })
}

// =============== User 模块 mock ===============

export function mockUserMe() {
  return ok({
    userId: 1001,
    username: 'fire_dev',
    email: 'fire_dev@qq.com',
    nickname: '火焰',
    avatarUrl: '',
    gender: 1,
    heightCm: 175,
    birthday: '1998-06-15',
    registeredAt: '2026-07-18 10:24:33',
    goal: {
      goalType: 1,
      goalTypeText: '减脂',
      targetWeightKg: 65.0,
      weeklyWorkoutDays: 4,
      kcalTarget: 1800,
      waterGoalMl: 2000
    }
  })
}
