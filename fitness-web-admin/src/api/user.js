import request from '@/utils/request'
import { USE_MOCK } from '@/config'
import * as mock from '@/mock/user'

/**
 * 用户模块接口（对齐后端 UserController）
 * - USE_MOCK=true（开发期）：走前端 mock
 * - USE_MOCK=false：走真实后端，Vite proxy /api → 8080
 * 成功响应：Result<T> = { code:200, message, data, timestamp }
 * request 拦截器已自动解包返回 data
 */

/** GET /user/profile —— 当前用户完整资料（user + user_profile 联查，嵌套 profile 对象） */
export function getMyProfile() {
  if (USE_MOCK) return mock.getMyProfile()
  return request.get('/api/v1/user/profile')
}

/** PUT /user/profile —— 更新基本资料（昵称/性别/生日/身高/体重/体脂/等级/简介/主题） */
export function updateMyProfile(data) {
  if (USE_MOCK) return mock.updateMyProfile(data)
  return request.put('/api/v1/user/profile', data)
}

/** PUT /user/account —— 更新账号信息（邮箱、手机号） */
export function updateMyAccount(data) {
  if (USE_MOCK) return mock.updateMyAccount(data)
  return request.put('/api/v1/user/account', data)
}

/** PUT /user/password —— 修改密码 body:{oldPassword,newPassword,confirmPassword} */
export function updateMyPassword(data) {
  if (USE_MOCK) return mock.updateMyPassword(data)
  return request.put('/api/v1/user/password', data)
}

/**
 * 训练统计概览（GET /user/stats）
 * 累计训练次数 / 总容量 / 当前连续天数 / 最近训练日期
 */
export function getMyTrainingStats() {
  if (USE_MOCK) return mock.getMyTrainingStats()
  return request.get('/api/v1/user/stats')
}

/**
 * 健康概览（GET /user/overview）
 * 最新体重/体脂 + 今日热量/饮水
 */
export function getMyHealthOverview() {
  if (USE_MOCK) return mock.getMyHealthOverview()
  return request.get('/api/v1/user/overview')
}

/**
 * 头像上传（POST /user/avatar）
 * multipart/form-data，字段 file；后端内部存储并回写 avatar_url
 * 返回 { avatarUrl }
 */
export function uploadAvatar(file) {
  if (USE_MOCK) return mock.uploadAvatar()
  const form = new FormData()
  form.append('file', file)
  return request.post('/api/v1/user/avatar', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
