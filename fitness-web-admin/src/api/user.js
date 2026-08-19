import request from '@/utils/request'
import { USE_MOCK } from '@/config'
import * as mock from '@/mock/user'

/**
 * 用户模块接口（对齐设计契约 §6.7 /user）
 * - USE_MOCK=true（开发期）：走前端 mock
 * - USE_MOCK=false：走真实后端，前缀 /api/v1
 * 成功响应：Result<T> = { code:200, message, data, timestamp }
 */

/** GET /user/me —— 当前用户资料 + 目标合并返回 */
export function getMyProfile() {
  if (USE_MOCK) return mock.getMyProfile()
  return request.get('/api/v1/user/me')
}

/** PUT /user/me —— 修改资料（昵称/头像/性别/生日/身高/简介） */
export function updateMyProfile(data) {
  if (USE_MOCK) return mock.updateMyProfile(data)
  return request.put('/api/v1/user/me', data)
}

/** PUT /user/me/password —— 修改密码 body:{oldPassword,newPassword,confirmPassword} */
export function updateMyPassword(data) {
  if (USE_MOCK) return mock.updateMyPassword(data)
  return request.put('/api/v1/user/me/password', data)
}

/** PUT /user/goal —— 修改目标 */
export function updateMyGoal(data) {
  if (USE_MOCK) return mock.updateMyGoal(data)
  return request.put('/api/v1/user/goal', data)
}
