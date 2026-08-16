import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const request = axios.create({
  baseURL: '/',
  timeout: 30000
})

request.interceptors.request.use(cfg => {
  const store = useUserStore()
  if (store.token) cfg.headers.Authorization = 'Bearer ' + store.token
  return cfg
})

request.interceptors.response.use(
  resp => {
    const body = resp.data
    if (!body || typeof body.code === 'undefined') return body // 非标准响应直接透传
    if (body.code === 200) return body.data
    if (body.code === 401 || body.code === 1002) {
      const store = useUserStore()
      store.token = ''; localStorage.removeItem('fitpulse_token')
      ElMessage.warning(body.message || '登录已过期')
      router.replace({ name: 'Login' })
      return Promise.reject(body)
    }
    ElMessage.error(body.message || '请求失败')
    return Promise.reject(body)
  },
  err => {
    if (err.response?.status === 401) {
      const store = useUserStore()
      store.token = ''; localStorage.removeItem('fitpulse_token')
      router.replace({ name: 'Login' })
    } else {
      ElMessage.error(err.response?.data?.message || err.message || '网络错误')
    }
    return Promise.reject(err)
  }
)

export default request
