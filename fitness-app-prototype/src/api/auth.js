// Auth 模块 API(对齐 docs/接口文档.md 二、Auth)
import { mockSendCode, mockLogin, mockRegister, mockForgotSendCode, mockForgotReset } from '@/mock'
import { mockCall } from '@/utils/request'

// 发送登录验证码 POST /auth/login/send-code  body:{email}
export function loginSendCode(email) {
  return mockCall(mockSendCode, email)
}

// 发送注册验证码 POST /auth/register/send-code  body:{email}
export function registerSendCode(email) {
  return mockCall(mockSendCode, email)
}

// 登录 POST /auth/login  body:{email,type,password?,code?}
export function login(data) {
  return mockCall(mockLogin, data)
}

// 注册 POST /auth/register  body:{email,password,code}
export function register(data) {
  return mockCall(mockRegister, data)
}

// 发送密码重置验证码 POST /auth/forgot-password/send-code  body:{email}
// 邮箱未注册返回 404,60s 内重复发送返回 409;成功回传 {code,expireMinutes,rateLimitSeconds}
export function forgotPasswordSendCode(email) {
  return mockCall(mockForgotSendCode, email)
}

// 重置密码 POST /auth/forgot-password/reset  body:{email,code,newPassword,confirmPassword}
// 校验通过后一次性消费验证码,不自动登录,前端跳转登录页
export function forgotPasswordReset(data) {
  return mockCall(mockForgotReset, data)
}
