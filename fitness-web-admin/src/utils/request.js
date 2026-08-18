import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

/**
 * Axios 实例
 * - baseURL '/' + vite proxy /api → 8080
 * - 成功：Result.code === 200，返回 body.data
 * - 401：尝试用 refreshToken 续签后重放；续签失败清凭证跳登录
 */
const request = axios.create({
  baseURL: '/',
  timeout: 30000
})

let isRefreshing = false           // 是否正在 refresh
let pendingQueue = []              // refresh 期间挂起的 401 请求

request.interceptors.request.use(cfg => {
  const store = useUserStore()
  if (store.token) cfg.headers.Authorization = 'Bearer ' + store.token
  return cfg
})

request.interceptors.response.use(
  resp => {
    const body = resp.data
    if (!body || typeof body.code === 'undefined') return body // 非标准响应透传
    if (body.code === 200) return body.data
    if (body.code === 401) return handle401(resp.config)
    ElMessage.error(body.message || '请求失败')
    return Promise.reject(body)
  },
  err => {
    if (err.response?.status === 401) return handle401(err.config)
    ElMessage.error(err.response?.data?.message || err.message || '网络错误')
    return Promise.reject(err)
  }
)

/**
 * 401 处理：refresh 续签 + 队列重放
 * - refresh 接口自身 401 直接失败，避免递归
 * - 正在 refresh 时，新 401 请求挂入队列
 */
async function handle401(originalConfig) {
  const store = useUserStore()
  if (originalConfig?.url?.includes('/auth/refresh')) {
    store.clearAuth()
    redirectToLogin()
    return Promise.reject(new Error('refresh 失败'))
  }
  if (!store.refreshToken) {
    store.clearAuth()
    redirectToLogin()
    return Promise.reject(new Error('未登录'))
  }
  if (isRefreshing) {
    return new Promise((resolve, reject) => {
      pendingQueue.push({ originalConfig, resolve, reject })
    })
  }
  isRefreshing = true
  try {
    await store.refresh()
    pendingQueue.forEach(({ originalConfig: cfg, resolve, reject }) => {
      request(cfg).then(resolve).catch(reject)
    })
    pendingQueue = []
    return request(originalConfig)
  } catch (e) {
    pendingQueue = []
    store.clearAuth()
    ElMessage.warning('登录已过期，请重新登录')
    redirectToLogin()
    return Promise.reject(e)
  } finally {
    isRefreshing = false
  }
}

function redirectToLogin() {
  if (router.currentRoute.value.name !== 'Login') {
    router.replace({
      name: 'Login',
      query: { redirect: router.currentRoute.value.fullPath }
    })
  }
}

export default request
