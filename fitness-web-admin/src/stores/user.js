import { defineStore } from 'pinia'
import {
  login as apiLogin,
  logout as apiLogout,
  refreshToken as apiRefresh
} from '@/api/auth'

/**
 * 用户认证状态（仅 auth 链路）
 * 持久化键：fitpulse_token / fitpulse_rt / fitpulse_user
 * LoginUserVO = { accessToken, refreshToken, userId, username }
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('fitpulse_token') || '',
    refreshToken: localStorage.getItem('fitpulse_rt') || '',
    userInfo: JSON.parse(localStorage.getItem('fitpulse_user') || 'null') || null
  }),
  getters: {
    isLogin: s => !!s.token,
    username: s => s.userInfo?.username || '未登录'
  },
  actions: {
    /** 登录：接收表单 {email,type,password?,code?}，内部调 API 并持久化 */
    async login(form) {
      const vo = await apiLogin(form)
      this.token = vo.accessToken
      this.refreshToken = vo.refreshToken
      this.userInfo = { userId: vo.userId, username: vo.username }
      localStorage.setItem('fitpulse_token', this.token)
      localStorage.setItem('fitpulse_rt', this.refreshToken)
      localStorage.setItem('fitpulse_user', JSON.stringify(this.userInfo))
      return vo
    },
    /** 用 refreshToken 换一对新 token（旋转失效） */
    async refresh() {
      if (!this.refreshToken) throw new Error('无 refreshToken')
      const vo = await apiRefresh(this.refreshToken)
      this.token = vo.accessToken
      this.refreshToken = vo.refreshToken
      this.userInfo = { userId: vo.userId, username: vo.username }
      localStorage.setItem('fitpulse_token', this.token)
      localStorage.setItem('fitpulse_rt', this.refreshToken)
      localStorage.setItem('fitpulse_user', JSON.stringify(this.userInfo))
      return vo
    },
    /** 登出：尽量通知后端销毁 refreshToken，失败也清本地 */
    async logout() {
      try { await apiLogout() } catch (_) {}
      this._clear()
    },
    /** 清空本地凭证（401 续签失败时调用） */
    clearAuth() { this._clear() },
    _clear() {
      this.token = ''
      this.refreshToken = ''
      this.userInfo = null
      localStorage.removeItem('fitpulse_token')
      localStorage.removeItem('fitpulse_rt')
      localStorage.removeItem('fitpulse_user')
    }
  }
})
