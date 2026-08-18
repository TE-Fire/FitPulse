import request from '@/utils/request'

/**
 * Auth 模块接口（与后端 AuthController 对齐）
 * 接口前缀：/api/v1/auth
 * 成功响应：Result<T> = { code:200, message, data, timestamp }
 */

/** 发送注册验证码 POST /auth/register/send-code  body:{email} */
export function registerSendCode(email) {
  return request.post('/api/v1/auth/register/send-code', { email })
}

/** 注册 POST /auth/register  body:{email,password,code} */
export function register(data) {
  return request.post('/api/v1/auth/register', data)
}

/** 发送登录验证码 POST /auth/login/send-code  body:{email} */
export function loginSendCode(email) {
  return request.post('/api/v1/auth/login/send-code', { email })
}

/**
 * 登录 POST /auth/login  body:{email,type,password?,code?}
 * type=1 密码登录（需 password）/ type=2 验证码登录（需 code）
 */
export function login(data) {
  return request.post('/api/v1/auth/login', data)
}

/** 刷新 Token POST /auth/refresh  body:{refreshToken} —— 旋转失效，返回新的一对 token */
export function refreshToken(refreshToken) {
  return request.post('/api/v1/auth/refresh', { refreshToken })
}

/** 登出 POST /auth/logout —— 需 Bearer accessToken（请求拦截器自动注入） */
export function logout() {
  return request.post('/api/v1/auth/logout')
}
