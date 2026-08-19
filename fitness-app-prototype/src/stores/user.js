import { defineStore } from 'pinia'
import * as authApi from '@/api/auth'
import * as userApi from '@/api/user'

const TOKEN_KEY = 'fitpulse_token'
const RT_KEY = 'fitpulse_rt'
const USER_KEY = 'fitpulse_user'
const LEGACY_TOKEN_KEY = 'fitpulse_access_token'

export const useUserStore = defineStore('user', {
  state: () => {
    // 旧版 fitpulse_access_token 一次性迁移到新键名(向后兼容),避免用户需重新登录
    const legacyAt = localStorage.getItem(LEGACY_TOKEN_KEY)
    if (legacyAt && !localStorage.getItem(TOKEN_KEY)) {
      localStorage.setItem(TOKEN_KEY, legacyAt)
    }
    localStorage.removeItem(LEGACY_TOKEN_KEY)
    const savedUser = JSON.parse(localStorage.getItem(USER_KEY) || 'null')
    return {
      accessToken: localStorage.getItem(TOKEN_KEY) || '',
      refreshToken: localStorage.getItem(RT_KEY) || '',
      userId: savedUser?.userId || '',
      username: savedUser?.username || '',
      userInfo: savedUser, // {userId, username},与 PC 端一致
      profile: null // 完整 UserProfileVO,由 loadMe() 填充
    }
  },
  getters: {
    isLoggedIn: (state) => !!state.accessToken
  },
  actions: {
    setToken(at, rt) {
      this.accessToken = at || ''
      this.refreshToken = rt || ''
      if (at) localStorage.setItem(TOKEN_KEY, at)
      else localStorage.removeItem(TOKEN_KEY)
      if (rt) localStorage.setItem(RT_KEY, rt)
      else localStorage.removeItem(RT_KEY)
    },
    setUserInfo(info) {
      this.userInfo = info
      if (info) {
        this.userId = info.userId || ''
        this.username = info.username || ''
        localStorage.setItem(USER_KEY, JSON.stringify(info))
      } else {
        this.userId = ''
        this.username = ''
        localStorage.removeItem(USER_KEY)
      }
    },
    clearAuth() {
      this.setToken('', '')
      this.setUserInfo(null)
      this.profile = null
    },
    async login(payload) {
      const data = await authApi.login(payload)
      this.setToken(data.accessToken, data.refreshToken)
      this.setUserInfo({ userId: data.userId, username: data.username })
      return data
    },
    async register(payload) {
      return authApi.register(payload)
    },
    async loadMe() {
      const data = await userApi.getProfile()
      this.profile = data
      // 与 login 同步,用 userId+username 更新基础 userInfo
      this.setUserInfo({ userId: data.userId, username: data.username })
      return data
    },
    async refresh() {
      if (!this.refreshToken) throw new Error('无刷新凭证,请重新登录')
      const data = await authApi.refreshToken(this.refreshToken)
      this.setToken(data.accessToken, data.refreshToken)
      return data
    },
    async logout() {
      // 后端 logout 失败不阻塞本地清凭证
      try { await authApi.logout() } catch (e) { /* ignore */ }
      this.clearAuth()
    },
    resetStore() {
      this.clearAuth()
    }
  }
})
