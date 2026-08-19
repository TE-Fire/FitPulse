// User 模块 API(对齐 docs/接口文档.md 三、User 接口)
// 与后端 UserController 一一对齐(路径严格匹配),全部需 Bearer accessToken
import request from '@/utils/request'

const PREFIX = '/api/v1/user'

/* ---------- 3.1 获取资料 GET /user/profile ---------- */

/**
 * 获取用户资料(含 user + user_profile 聚合)
 * profile 尚未建立时 profile 字段为空对象
 */
export function getProfile() {
  return request.get(`${PREFIX}/profile`)
}

/* ---------- 3.2 更新资料 PUT /user/profile ---------- */

/**
 * 更新基本资料(部分更新,字段传 null = 清空原值,不存在自动 upsert)
 * @param {{nickname?:string|null, avatarUrl?:string|null, gender?:'MALE'|'FEMALE'|null, birthday?:string|null, heightCm?:number|null, weightKg?:number|null, bodyFatPct?:number|null, fitnessLevel?:string|null, theme?:string|null, bio?:string|null}} data
 */
export function updateProfile(data) {
  return request.put(`${PREFIX}/profile`, data)
}

/* ---------- 3.3 更新账号 PUT /user/account ---------- */

/**
 * 更新账号字段(邮箱/手机号),邮箱冲突返回 409
 * @param {{email?:string, phone?:string}} data
 */
export function updateAccount(data) {
  return request.put(`${PREFIX}/account`, data)
}

/* ---------- 3.4 修改密码 PUT /user/password ---------- */

/**
 * 修改密码: 两次不一致 400,旧密码错误 401
 * @param {{oldPassword:string, newPassword:string, confirmPassword:string}} data
 */
export function changePassword(data) {
  return request.put(`${PREFIX}/password`, data)
}

/* ---------- 3.5 上传头像 POST /user/avatar ---------- */

/**
 * 上传头像(multipart/form-data),后端走通用上传并写入 profile.avatarUrl
 * @param {Blob|File} file 图片文件
 * @returns {{avatarUrl:string}} 可访问的头像 URL
 */
export function uploadAvatar(file) {
  const fd = new FormData()
  fd.append('file', file)
  return request.post(`${PREFIX}/avatar`, fd)
}

/* ---------- 3.6 训练统计概览 GET /user/stats ---------- */

/**
 * 获取训练统计概览
 * @returns {TrainingStatsVO} 连续训练天数/总容量/累计训练时长等
 */
export function getTrainingStats() {
  return request.get(`${PREFIX}/stats`)
}

/* ---------- 3.7 健康概览 GET /user/overview ---------- */

/**
 * 获取健康概览(最新体重/体脂率/热量摄入等指标)
 * @returns {HealthOverviewVO}
 */
export function getHealthOverview() {
  return request.get(`${PREFIX}/overview`)
}
