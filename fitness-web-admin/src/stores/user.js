import { defineStore } from 'pinia'
import { login as apiLogin, logout as apiLogout, getUserProfile } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('fitpulse_token') || '',
    refreshToken: localStorage.getItem('fitpulse_rt') || '',
    userInfo: JSON.parse(localStorage.getItem('fitpulse_user') || 'null') || null
  }),
  getters: {
    isLogin: s => !!s.token,
    username: s => s.userInfo?.username || '未登录',
    nickname: s => s.userInfo?.nickname || 'FitPulse 用户',
    avatar: s => s.userInfo?.avatarUrl
  },
  actions: {
    async login(form) {
      const r = await apiLogin(form)
      this.token = r.accessToken
      this.refreshToken = r.refreshToken
      this.userInfo = { userId: r.userId, username: r.username, nickname: r.nickname, avatarUrl: r.avatarUrl }
      localStorage.setItem('fitpulse_token', this.token)
      localStorage.setItem('fitpulse_rt', this.refreshToken)
      localStorage.setItem('fitpulse_user', JSON.stringify(this.userInfo))
      return r
    },
    async logout() {
      try { await apiLogout() } catch(e) {}
      this.token = ''; this.refreshToken = ''; this.userInfo = null
      localStorage.removeItem('fitpulse_token')
      localStorage.removeItem('fitpulse_rt')
      localStorage.removeItem('fitpulse_user')
    },
    async loadProfile() {
      try {
        const r = await getUserProfile()
        this.userInfo = { ...this.userInfo, ...r }
        localStorage.setItem('fitpulse_user', JSON.stringify(this.userInfo))
      } catch(e) {}
    }
  }
})
