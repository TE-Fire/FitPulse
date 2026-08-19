<template>
  <div class="fp-tab">
    <!-- 主题切换 -->
    <div class="fp-section-title">外观主题</div>
    <div class="fp-theme-grid">
      <button
        v-for="opt in themeOptions"
        :key="opt.value"
        class="fp-theme-card"
        :class="{ 'is-active': themeStore.mode === opt.value }"
        @click="themeStore.set(opt.value)"
      >
        <div class="fp-theme-card__preview" :class="`preview--${opt.value}`">
          <span class="bar" />
          <span class="dot" />
        </div>
        <div class="fp-theme-card__body">
          <strong>{{ opt.label }}</strong>
          <small>{{ opt.desc }}</small>
        </div>
        <el-icon v-if="themeStore.mode === opt.value" class="fp-theme-card__check"><CircleCheckFilled /></el-icon>
      </button>
    </div>

    <!-- 缓存管理 -->
    <div class="fp-section-title" style="margin-top: 24px;">缓存管理</div>
    <div class="fp-card fp-cache-card">
      <div>
        <strong>清除本地缓存</strong>
        <p>清除 localStorage 中的非鉴权数据（保留登录态），用于刷新本地设置/主题缓存。</p>
      </div>
      <el-button type="danger" plain @click="onClearCache">
        <el-icon><Delete /></el-icon><span>清除缓存</span>
      </el-button>
    </div>

    <!-- 实际生效信息 -->
    <div class="fp-card fp-info-card">
      <el-icon><InfoFilled /></el-icon>
      <span>当前实际生效：<strong>{{ themeStore.resolved === 'dark' ? '深色' : '浅色' }}</strong>（{{ themeStore.mode === 'auto' ? '跟随系统' : '手动指定' }}）</span>
    </div>
  </div>
</template>

<script setup>
/**
 * 设置 Tab
 * - 主题 3 态选择器（light / dark / auto）
 * - 清除缓存（保留 fitpulse_token / fitpulse_rt / fitpulse_user）
 */
import { ElMessage, ElMessageBox } from 'element-plus'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()

const themeOptions = [
  { value: 'light', label: '浅色',  desc: '明亮清晰，适合白天' },
  { value: 'dark',  label: '深色',  desc: '护眼省电，适合夜晚' },
  { value: 'auto',  label: '跟随系统', desc: '根据系统设置自动切换' }
]

const KEEP_KEYS = ['fitpulse_token', 'fitpulse_rt', 'fitpulse_user', 'fitpulse_theme']

async function onClearCache() {
  try {
    await ElMessageBox.confirm('确认清除本地缓存？登录态与主题偏好将被保留。', '清除缓存', {
      type: 'warning',
      confirmButtonText: '清除',
      cancelButtonText: '取消'
    })
    Object.keys(localStorage)
      .filter(k => !KEEP_KEYS.includes(k))
      .forEach(k => localStorage.removeItem(k))
    ElMessage.success('本地缓存已清除')
  } catch (e) {
    // 用户取消，不做处理
  }
}
</script>

<style scoped>
.fp-tab { padding: 4px 0; }

.fp-theme-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 14px;
}
.fp-theme-card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px;
  border-radius: 12px;
  border: 2px solid var(--border);
  background: var(--card);
  cursor: pointer;
  transition: all 0.15s ease;
  text-align: left;
  color: inherit;
  font: inherit;
}
.fp-theme-card:hover { border-color: var(--fit-brand); transform: translateY(-2px); }
.fp-theme-card.is-active { border-color: var(--fit-brand); box-shadow: 0 4px 14px -4px rgba(124,92,255,0.45); }

.fp-theme-card__preview {
  height: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 12px;
  border: 1px solid var(--border);
}
.fp-theme-card__preview .bar { flex: 1; height: 6px; border-radius: 3px; }
.fp-theme-card__preview .dot { width: 12px; height: 12px; border-radius: 50%; }
.preview--light { background: #f8fafc; }
.preview--light .bar { background: #7c5cff; }
.preview--light .dot { background: #22d3ee; }
.preview--dark { background: #0b1020; }
.preview--dark .bar { background: #8b7bff; }
.preview--dark .dot { background: #22d3ee; }
.preview--auto {
  background: linear-gradient(90deg, #f8fafc 0%, #f8fafc 50%, #0b1020 50%, #0b1020 100%);
}
.preview--auto .bar { background: linear-gradient(90deg, #7c5cff 50%, #8b7bff 50%); }
.preview--auto .dot { background: #22d3ee; }

.fp-theme-card__body { display: flex; flex-direction: column; gap: 2px; }
.fp-theme-card__body strong { font-size: 14px; color: var(--text); }
.fp-theme-card__body small { font-size: 12px; color: var(--text-muted); }
.fp-theme-card__check {
  position: absolute;
  top: 10px; right: 10px;
  color: var(--fit-brand);
  font-size: 20px;
}

.fp-cache-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
}
.fp-cache-card strong { display: block; font-size: 14px; color: var(--text); margin-bottom: 4px; }
.fp-cache-card p { margin: 0; font-size: 12px; color: var(--text-muted); max-width: 480px; }

.fp-info-card {
  margin-top: 12px;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-soft);
}
.fp-info-card .el-icon { color: var(--fit-brand); }
.fp-info-card strong { color: var(--fit-brand); }
</style>
