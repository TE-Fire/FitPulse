// app-prototype 请求工具
// 两种调用方式：
//   1) 默认导出 request: axios 实例,走真实后端(vite proxy /api -> localhost:8080)
//        使用方: import request from '@/utils/request'
//   2) 命名导出 mockCall: 同步调用 mock 函数,解包 Result,失败抛 Error
//        使用方: import { mockCall } from '@/utils/request'
//
// 注意: 本模块不引 Element Plus,错误通过原生 alert 或抛出Error交给调用方
import axios from 'axios'
import * as mockMod from '@/mock'

// ---------- 持久化键名(与 stores/user.js 保持一致) ----------
const TOKEN_KEY = 'fitpulse_token'
const RT_KEY = 'fitpulse_rt'

// ---------- axios 实例:真实后端 ----------
const request = axios.create({
  baseURL: '',
  timeout: 30000
})

// 401 续签状态 + 等待队列(与 PC 端一致的雪崩防护机制)
let isRefreshing = false
let pendingQueue = []

function enqueueAndNotify(resolve, retryFn) {
  pendingQueue.push({ resolve, retry: retryFn })
}
function flushQueue(err) {
  const q = pendingQueue.slice()
  pendingQueue = []
  q.forEach(({ resolve, retry }) => {
    if (err) resolve(Promise.reject(err))
    else resolve(retry())
  })
}

// 请求拦截器: 注入 Bearer accessToken
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(TOKEN_KEY)
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (err) => Promise.reject(err)
)

// 响应拦截器: 解包 Result,处理 401 refresh 续签
request.interceptors.response.use(
  (response) => {
    const body = response.data
    // 非 JSON / 无 code 的响应直接原样返回(例如纯文本/图片)
    if (!body || typeof body !== 'object' || typeof body.code === 'undefined') return body
    if (body.code === 200) return body.data
    // 业务错误统一抛 Error(message),调用方 try/catch 时可直接读取 .message
    return Promise.reject(new Error(body.message || `请求失败(${body.code})`))
  },
  async (error) => {
    const resp = error.response
    // 非 401 网络错误直接抛
    if (!resp) {
      const msg = error.message === 'Network Error' ? '网络错误,请确认后端服务启动且端口 8080 可访问' : (error.message || '请求异常')
      return Promise.reject(new Error(msg))
    }
    if (resp.status !== 401) {
      // 后端业务错误会带 JSON Body(code+message),优先读 body.message
      const body = resp.data
      const msg = body && body.message ? body.message : `HTTP ${resp.status}`
      return Promise.reject(new Error(msg))
    }

    // ----- 401: 触发 refresh 续签 -----
    const originalConfig = error.config
    // 登录接口本身 401 不续签,直接抛错
    if (originalConfig.url && /\/auth\/(login|refresh)/.test(originalConfig.url)) {
      return Promise.reject(new Error(resp.data?.message || '认证失败'))
    }
    // 已在续签:当前请求入队,等续签完成后重试
    if (isRefreshing) {
      return new Promise((resolve) => {
        enqueueAndNotify(resolve, () => request(originalConfig))
      })
    }
    // 无 refreshToken,直接清凭证 + 跳登录
    const rt = localStorage.getItem(RT_KEY)
    if (!rt) {
      clearAndRedirectToLogin(resp.data?.message || '登录状态已失效')
      return Promise.reject(new Error(resp.data?.message || '登录状态已失效'))
    }
    // 发起续签
    isRefreshing = true
    try {
      // 动态 import 避免 api/auth.js ↔ request.js 循环依赖
      const { refreshToken } = await import('@/api/auth')
      const data = await refreshToken(rt)
      const { accessToken, refreshToken: newRt } = data || {}
      if (accessToken && newRt) {
        localStorage.setItem(TOKEN_KEY, accessToken)
        localStorage.setItem(RT_KEY, newRt)
        // 用新 token 重放当前请求 + 队列中所有请求
        if (originalConfig.headers) originalConfig.headers.Authorization = `Bearer ${accessToken}`
        flushQueue(null)
        return request(originalConfig)
      } else {
        // 续签返回无 token,按失败处理
        throw new Error(resp.data?.message || '登录状态已失效')
      }
    } catch (refreshErr) {
      // 续签失败:清全部凭证 + 队列全部 reject + 跳登录
      const finalMsg = refreshErr.message || '登录状态已失效,请重新登录'
      clearAndRedirectToLogin(finalMsg)
      flushQueue(refreshErr)
      return Promise.reject(refreshErr)
    } finally {
      isRefreshing = false
    }
  }
)

function clearAndRedirectToLogin(msg) {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(RT_KEY)
  localStorage.removeItem('fitpulse_user')
  try {
    alert(msg)
  } catch (e) { /* noop */ }
  // 不引 router,避免进一步循环依赖;用 location 强制跳登录
  const base = (import.meta.env.BASE_URL || '/').replace(/\/$/, '') || ''
  window.location.href = `${base}/#/login`
}

// ---------- mockCall: dashboard/ai 等未开发模块继续走 mock ----------
export function mockCall(fn, ...args) {
  const result = fn(...args)
  if (result && typeof result === 'object' && typeof result.code !== 'undefined') {
    if (result.code === 200) return result.data
    const msg = result.message || `请求失败(${result.code})`
    // 对齐 request.js: 401 时也清凭证跳登录
    if (result.code === 401) clearAndRedirectToLogin(msg)
    throw new Error(msg)
  }
  return result
}

export { mockMod }
export default request
