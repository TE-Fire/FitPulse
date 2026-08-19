<template>
  <div class="fp-tab">
    <!-- 应用信息 -->
    <div class="fp-card fp-about-card">
      <div class="fp-about-logo">
        <el-icon :size="36" color="#fff">
          <svg viewBox="0 0 24 24" width="36" height="36" fill="currentColor">
            <path d="M12 21s-6.7-4.3-9.2-8.5C1.2 9.7 2.4 6 5.8 6c2 0 3.4 1.2 4.2 2.4C10.8 7.2 12.2 6 14.2 6c3.4 0 4.6 3.7 3 6.5C18.7 16.7 12 21 12 21z"/>
          </svg>
        </el-icon>
      </div>
      <div class="fp-about-text">
        <h2>FitPulse</h2>
        <p>个人健康健身一体化管理后台</p>
        <div class="fp-about-version">
          <el-tag effect="light" type="success">v1.0.0</el-tag>
          <span class="fp-hint">构建时间 2026-08-19</span>
        </div>
      </div>
    </div>

    <!-- 技术栈 -->
    <div class="fp-section-title">技术栈</div>
    <div class="fp-stack-grid">
      <span v-for="t in stack" :key="t" class="fp-stack-tag">{{ t }}</span>
    </div>

    <!-- 项目描述 -->
    <div class="fp-section-title" style="margin-top: 20px;">项目说明</div>
    <div class="fp-card fp-desc-card">
      <p>FitPulse 是一款面向个人用户的健康健身 App，提供训练打卡、身体数据记录、饮食与饮水、AI 顾问等一体化能力。</p>
      <p>本管理后台基于 <strong>Vue 3 + Vite + Element Plus + ECharts + Tailwind CSS</strong> 构建，看板与个人中心通过统一设计契约与后端、移动端保持字段一致性。</p>
      <p class="fp-hint">设计契约依据：docs/设计契约.md（冻结版）</p>
    </div>

    <!-- 退出登录 -->
    <div class="fp-card fp-logout-card">
      <div>
        <strong>退出登录</strong>
        <p>清除本地登录态并返回登录页</p>
      </div>
      <el-button type="danger" :loading="logging" @click="onLogout">
        <el-icon><SwitchButton /></el-icon><span>退出登录</span>
      </el-button>
    </div>
  </div>
</template>

<script setup>
/**
 * 关于 Tab
 * - 应用信息（版本、构建时间）
 * - 技术栈标签
 * - 项目说明
 * - 退出登录按钮
 */
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const logging = ref(false)

const stack = [
  'Vue 3.4', 'Vite 5', 'Pinia', 'Vue Router 4',
  'Element Plus', 'Tailwind CSS', 'ECharts', 'vue-echarts',
  'Axios', 'Day.js', 'NProgress'
]

async function onLogout() {
  logging.value = true
  try {
    await userStore.logout()
    ElMessage.success('已退出登录')
    router.replace({ name: 'Login' })
  } catch (e) {
    ElMessage.error('退出失败，请重试')
  } finally {
    logging.value = false
  }
}
</script>

<style scoped>
.fp-tab { padding: 4px 0; }

.fp-about-card {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 24px 28px;
  margin-bottom: 18px;
}
.fp-about-logo {
  width: 64px; height: 64px;
  border-radius: 16px;
  background: linear-gradient(135deg, #7c5cff, #22d3ee);
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 8px 18px -4px rgba(124,92,255,0.55);
  flex-shrink: 0;
}
.fp-about-text h2 { margin: 0; font-size: 24px; color: var(--text); }
.fp-about-text p { margin: 4px 0 8px; font-size: 13px; color: var(--text-soft); }
.fp-about-version {
  display: flex;
  align-items: center;
  gap: 8px;
}
.fp-hint { color: var(--text-muted); font-size: 12px; }

.fp-stack-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.fp-stack-tag {
  padding: 4px 10px;
  border-radius: 6px;
  background: var(--bg-soft);
  color: var(--text-soft);
  font-size: 12px;
  border: 1px solid var(--border);
}

.fp-desc-card {
  padding: 16px 20px;
}
.fp-desc-card p {
  margin: 0 0 10px;
  font-size: 13px;
  color: var(--text-soft);
  line-height: 1.7;
}
.fp-desc-card p:last-child { margin-bottom: 0; }
.fp-desc-card strong { color: var(--text); }

.fp-logout-card {
  margin-top: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border-color: rgba(239, 68, 68, 0.25);
}
.fp-logout-card strong { display: block; font-size: 14px; color: var(--text); margin-bottom: 4px; }
.fp-logout-card p { margin: 0; font-size: 12px; color: var(--text-muted); }
</style>
