// 用户状态(简化版,用 localStorage 手动持久化,不引入 persist 插件)
import { defineStore } from 'pinia'
import * as authApi from '@/api/auth'

const TOKEN_KEY = 'fitpulse_access_token'

export const useUserStore = defineStore('user', {
  state: () => ({
    accessToken: localStorage.getItem(TOKEN_KEY) || '',
    refreshToken: '',
    userId: null,
    username: ''
  }),
  getters: {
    isLoggedIn: (state) => !!state.accessToken
  },
  actions: {
    async login(payload) {
      const data = await authApi.login(payload)
      this.accessToken = data.accessToken
      this.refreshToken = data.refreshToken
      this.userId = data.userId
      this.username = data.username
      localStorage.setItem(TOKEN_KEY, data.accessToken)
      return data
    },
    logout() {
      this.accessToken = ''
      this.refreshToken = ''
      this.userId = null
      this.username = ''
      localStorage.removeItem(TOKEN_KEY)
    }
  }
})
