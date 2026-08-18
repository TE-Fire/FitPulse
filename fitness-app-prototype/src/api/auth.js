// Auth 模块 API(对齐 docs/接口文档.md 二、Auth)
import { mockSendCode, mockLogin, mockRegister } from '@/mock'
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
