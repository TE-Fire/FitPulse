import { defineStore } from 'pinia'

/**
 * 主题状态：light / dark / auto
 * - light  → 强制浅色
 * - dark   → 强制深色
 * - auto   → 跟随系统 prefers-color-scheme
 *
 * 应用方式：在 <html> 上加/移除 class="dark"，CSS 变量自动切换
 * 持久化键：fitpulse_theme
 */
const VALID = ['light', 'dark', 'auto']
const STORAGE_KEY = 'fitpulse_theme'

let media = null
let mediaListener = null

export const useThemeStore = defineStore('theme', {
  state: () => ({
    mode: readMode()
  }),
  getters: {
    /** auto 模式下解析为当前实际生效值 */
    resolved: s => (s.mode === 'auto' ? systemPrefersDark() ? 'dark' : 'light' : s.mode),
    isDark: s => s.resolved === 'dark',
    isAuto: s => s.mode === 'auto'
  },
  actions: {
    /** 应用当前 mode 到 <html>，并维护 auto 模式的事件监听 */
    apply() {
      // 清理旧监听
      if (mediaListener) {
        media.removeEventListener('change', mediaListener)
        mediaListener = null
      }
      // auto：监听系统主题变化实时刷新 <html> class
      if (this.mode === 'auto') {
        media = window.matchMedia('(prefers-color-scheme: dark)')
        mediaListener = () => applyClass(this.resolved)
        media.addEventListener('change', mediaListener)
      }
      applyClass(this.resolved)
    },
    /** 三态循环：light → dark → auto → light（顶栏按钮用） */
    cycle() {
      const order = ['light', 'dark', 'auto']
      const idx = order.indexOf(this.mode)
      this.set(order[(idx + 1) % order.length])
    },
    /** 兼容旧 API：toggle 等价于 light ↔ dark（保留以免破坏旧调用） */
    toggle() {
      this.set(this.resolved === 'dark' ? 'light' : 'dark')
    },
    /** 直接设置主题（light/dark/auto） */
    set(mode) {
      if (!VALID.includes(mode)) return
      this.mode = mode
      localStorage.setItem(STORAGE_KEY, mode)
      this.apply()
    }
  }
})

function readMode() {
  const m = localStorage.getItem(STORAGE_KEY)
  return VALID.includes(m) ? m : 'light'
}

function systemPrefersDark() {
  return window.matchMedia?.('(prefers-color-scheme: dark)').matches ?? false
}

function applyClass(resolvedMode) {
  const el = document.documentElement
  if (resolvedMode === 'dark') el.classList.add('dark')
  else el.classList.remove('dark')
}
