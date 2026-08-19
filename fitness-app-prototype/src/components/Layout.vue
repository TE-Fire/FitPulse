<template>
  <!-- App Shell:Layout 承担页面主区域 + 固定底部 BottomNav 的职责 -->
  <!-- Layout 内部嵌套 router-view 也加 :key,避免同级 tab 切换时因 diff 丢失响应式 -->
  <div class="layout-shell">
    <main class="layout-main">
      <router-view v-slot="{ Component, route }">
        <transition name="fade">
          <component :is="Component" :key="route.fullPath" />
        </transition>
      </router-view>
    </main>

    <nav class="bottom-nav">
      <router-link to="/home" class="nav-item" active-class="active">
        <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M6.5 6.5l11 11" />
          <path d="M21 21l-1-1" />
          <path d="M3 3l1 1" />
          <path d="M18 22l4-4" />
          <path d="M2 6l4-4" />
          <path d="M3 10l7-7" />
          <path d="M14 21l7-7" />
        </svg>
        <span>训练</span>
      </router-link>

      <router-link to="/health" class="nav-item" active-class="active">
        <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M22 12h-4l-3 9L9 3l-3 9H2" />
        </svg>
        <span>健康</span>
      </router-link>

      <router-link to="/ai" class="nav-item" active-class="active">
        <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 8V4H8" />
          <rect width="16" height="12" x="4" y="8" rx="2" />
          <path d="M2 14h2" />
          <path d="M20 14h2" />
          <path d="M15 13v2" />
          <path d="M9 13v2" />
        </svg>
        <span>AI 教练</span>
      </router-link>

      <router-link to="/profile" class="nav-item" active-class="active">
        <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" />
          <circle cx="12" cy="7" r="4" />
        </svg>
        <span>我的</span>
      </router-link>
    </nav>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

// Layout 含 BottomNav,固定底部 4 个 tab(对齐设计契约 4.2 BottomNav 列表)
// 进入需要登录的 Layout 时,拉取最新 profile 填充 store
const userStore = useUserStore()
onMounted(async () => {
  if (userStore.isLoggedIn && !userStore.profile) {
    try {
      await userStore.loadMe()
    } catch (e) {
      // 401 会走 request.js 拦截器清凭证跳登录,此处静默即可
    }
  }
})
</script>

<style scoped>
.layout-shell {
  min-height: 100vh;
  background: #f6f7fb;
}
.layout-main {
  min-height: 100vh;
}
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 420px;
  height: 64px;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  z-index: 50;
  padding-bottom: env(safe-area-inset-bottom, 0px);
}
@media (min-width: 768px) {
  .bottom-nav {
    max-width: 390px;
    border-radius: 0 0 28px 28px;
  }
}
.nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #94a3b8;
  text-decoration: none;
  font-size: 11px;
  font-weight: 500;
  transition: color 0.15s ease;
}
.nav-icon {
  width: 22px;
  height: 22px;
}
.nav-item.active {
  color: #7c5cff;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.18s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
