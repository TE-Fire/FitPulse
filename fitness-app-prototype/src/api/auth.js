// Auth 模块 API(对齐 docs/接口文档.md 二、Auth)
// 全部切真实后端接口(不再走 mock)
import request from '@/utils/request'

// 接口统一前缀(后端 @RequestMapping("/auth"),与全局 /api/v1 前缀拼合为 /api/v1/auth)
const PREFIX = '/api/v1/auth'

/* ---------- 2.1 / 2.2 发送验证码 ---------- */

/**
 * 发送注册验证码 POST /auth/register/send-code
 * @param {string} email 邮箱
 * @returns {{code:string, expireMinutes:number, rateLimitSeconds:number}} 开发环境后端返回明文验证码
 */
export function registerSendCode(email) {
  return request.post(`${PREFIX}/register/send-code`, { email })
}

/**
 * 发送登录验证码 POST /auth/login/send-code
 * @param {string} email 邮箱
 */
export function loginSendCode(email) {
  return request.post(`${PREFIX}/login/send-code`, { email })
}

/* ---------- 2.3 注册 ---------- */

/**
 * 注册 POST /auth/register
 * @param {{email:string, password:string, code:string}} data
 */
export function register(data) {
  return request.post(`${PREFIX}/register`, data)
}

/* ---------- 2.4 登录 ---------- */

/**
 * 登录 POST /auth/login
 * @param {{email:string, type:1|2, password?:string, code?:string}} data type=1密码 type=2验证码
 */
export function login(data) {
  return request.post(`${PREFIX}/login`, data)
}

/* ---------- 2.5 刷新 Token ---------- */

/**
 * 刷新 Token POST /auth/refresh
 * @param {string} refreshToken
 * @returns {{accessToken:string, refreshToken:string}} 续签后的双 token
 */
export function refreshToken(refreshToken) {
  return request.post(`${PREFIX}/refresh`, { refreshToken })
}

/* ---------- 2.6 退出登录 ---------- */

/**
 * 退出登录 POST /auth/logout
 */
export function logout() {
  return request.post(`${PREFIX}/logout`)
}

/* ---------- 2.7 / 2.8 忘记密码 ---------- */

/**
 * 发送密码重置验证码 POST /auth/forgot-password/send-code
 * 邮箱未注册返回 404,60s 内重复发送返回 409
 * @param {string} email
 */
export function forgotPasswordSendCode(email) {
  return request.post(`${PREFIX}/forgot-password/send-code`, { email })
}

/**
 * 重置密码 POST /auth/forgot-password/reset
 * 不自动登录,前端跳登录页
 */
export function forgotPasswordReset({ email, code, newPassword, confirmPassword }) {
  return request.post(`${PREFIX}/forgot-password/reset`, { email, code, newPassword, confirmPassword })
}
