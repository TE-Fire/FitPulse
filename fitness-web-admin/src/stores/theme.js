import { defineStore } from 'pinia'

/**
 * 主题状态：light / dark
 * - 持久化键：fitpulse_theme
 * - 应用方式：在 <html> 上加/移除 class="dark"，CSS 变量自动切换
 */
export const useThemeStore = defineStore('theme', {
  state: () => ({
    mode: localStorage.getItem('fitpulse_theme') || 'light'
  }),
  getters: {
    isDark: s => s.mode === 'dark'
  },
  actions: {
    /** 应用当前 mode 到 <html> */
    apply() {
      const el = document.documentElement
      if (this.mode === 'dark') el.classList.add('dark')
      else el.classList.remove('dark')
    },
    /** 切换主题并持久化 */
    toggle() {
      this.mode = this.mode === 'dark' ? 'light' : 'dark'
      localStorage.setItem('fitpulse_theme', this.mode)
      this.apply()
    },
    /** 直接设置主题 */
    set(mode) {
      if (mode !== 'light' && mode !== 'dark') return
      this.mode = mode
      localStorage.setItem('fitpulse_theme', this.mode)
      this.apply()
    }
  }
})
