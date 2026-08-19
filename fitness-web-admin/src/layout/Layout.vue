<template>
  <div class="fp-layout">
    <!-- 侧边栏 -->
    <aside class="fp-sidebar">
      <div class="fp-brand">
        <div class="fp-brand__logo">
          <el-icon><svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M12 21s-6.7-4.3-9.2-8.5C1.2 9.7 2.4 6 5.8 6c2 0 3.4 1.2 4.2 2.4C10.8 7.2 12.2 6 14.2 6c3.4 0 4.6 3.7 3 6.5C18.7 16.7 12 21 12 21z"/></svg></el-icon>
        </div>
        <div class="fp-brand__name">
          <strong>FitPulse</strong>
          <span>管理后台</span>
        </div>
      </div>

      <nav class="fp-nav">
        <p class="fp-nav__group">看板</p>
        <router-link
          v-for="item in navGroups.dashboard"
          :key="item.path"
          :to="item.path"
          class="fp-nav__item"
          active-class="is-active"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </router-link>

        <p class="fp-nav__group">管理</p>
        <router-link
          v-for="item in navGroups.profile"
          :key="item.path"
          :to="item.path"
          class="fp-nav__item"
          active-class="is-active"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </router-link>

        <p class="fp-nav__group fp-nav__group--muted">规划中（待开发）</p>
        <a
          v-for="item in navGroups.coming"
          :key="item.label"
          class="fp-nav__item is-disabled"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
          <small>{{ item.badge }}</small>
        </a>
      </nav>
    </aside>

    <!-- 主区 -->
    <div class="fp-main">
      <header class="fp-topbar">
        <div class="fp-breadcrumb">
          <span>{{ currentGroup }}</span>
          <i class="fp-sep">/</i>
          <strong>{{ currentTitle }}</strong>
        </div>
        <div class="fp-actions">
          <button class="fp-icon-btn" title="切换主题" @click="themeStore.toggle()">
            <el-icon><Moon v-show="!themeStore.isDark" /><Sunny v-show="themeStore.isDark" /></el-icon>
          </button>
          <el-dropdown trigger="click" @command="onUserCommand">
            <div class="fp-user">
              <span class="fp-user__avatar">{{ avatarLetter }}</span>
              <span class="fp-user__name">{{ userStore.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="fp-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useThemeStore } from '@/stores/theme'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const themeStore = useThemeStore()
const userStore = useUserStore()

const navGroups = {
  dashboard: [
    { path: '/dashboard/training', label: '训练看板', icon: 'TrendCharts' },
    { path: '/dashboard/health',   label: '健康看板', icon: 'DataLine' }
  ],
  profile: [
    { path: '/profile', label: '个人中心', icon: 'User' }
  ],
  coming: [
    { label: '动作库管理', icon: 'Files',   badge: '待开发' },
    { label: '训练计划',   icon: 'Calendar', badge: '待开发' },
    { label: '训练记录',   icon: 'List',     badge: '待开发' },
    { label: '身体数据',   icon: 'Monitor',  badge: '待开发' },
    { label: '饮食管理',   icon: 'Food',     badge: '待开发' },
    { label: '饮水日志',   icon: 'Coffee',   badge: '待开发' },
    { label: 'AI 对话',    icon: 'ChatDotRound', badge: '待开发' }
  ]
}

const currentGroup = computed(() => {
  if (route.path.startsWith('/dashboard')) return '看板'
  if (route.path.startsWith('/profile'))   return '管理'
  return '工作台'
})
const currentTitle = computed(() => route.meta.title || route.name || '')
const avatarLetter = computed(() => (userStore.username || 'F').charAt(0).toUpperCase())

async function onUserCommand(cmd) {
  if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'logout') {
    await userStore.logout()
    ElMessage.success('已退出登录')
    router.replace({ name: 'Login' })
  }
}
</script>

<style scoped>
.fp-layout {
  display: flex;
  min-height: 100vh;
  background: var(--bg);
  color: var(--text);
}

/* ---------- 侧边栏 ---------- */
.fp-sidebar {
  width: 232px;
  flex: 0 0 232px;
  background: var(--card);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
  overflow-y: auto;
}
.fp-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 18px 18px 12px;
}
.fp-brand__logo {
  width: 36px; height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #7c5cff, #22d3ee);
  color: #fff;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 6px 14px -4px rgba(124, 92, 255, 0.55);
}
.fp-brand__name { display: flex; flex-direction: column; line-height: 1.2; }
.fp-brand__name strong { font-size: 16px; color: var(--text); }
.fp-brand__name span { font-size: 11px; color: var(--text-muted); margin-top: 2px; }

.fp-nav { padding: 8px 10px 20px; flex: 1; }
.fp-nav__group {
  margin: 14px 12px 6px;
  font-size: 10px;
  color: var(--text-muted);
  letter-spacing: 0.08em;
  text-transform: uppercase;
}
.fp-nav__group--muted { opacity: 0.7; }
.fp-nav__item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  margin: 2px 0;
  border-radius: 10px;
  color: var(--text-soft);
  font-size: 14px;
  cursor: pointer;
  transition: background 0.15s ease, color 0.15s ease;
  text-decoration: none;
}
.fp-nav__item:hover { background: var(--bg-soft); color: var(--text); }
.fp-nav__item.is-active {
  background: linear-gradient(90deg, rgba(124, 92, 255, 0.12), transparent);
  color: var(--fit-brand);
  font-weight: 600;
}
.fp-nav__item.is-disabled {
  opacity: 0.5;
  cursor: not-allowed;
  pointer-events: none;
}
.fp-nav__item small {
  margin-left: auto;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 999px;
  background: var(--bg-soft);
  color: var(--text-muted);
}

/* ---------- 主区 ---------- */
.fp-main { flex: 1; display: flex; flex-direction: column; min-width: 0; }
.fp-topbar {
  height: 56px;
  flex: 0 0 56px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--card);
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 10;
}
.fp-breadcrumb {
  display: flex; align-items: center; gap: 8px;
  font-size: 13px; color: var(--text-muted);
}
.fp-breadcrumb strong { color: var(--text); font-size: 15px; }
.fp-sep { color: var(--text-muted); }

.fp-actions { display: flex; align-items: center; gap: 10px; }
.fp-icon-btn {
  width: 34px; height: 34px;
  border-radius: 8px;
  border: 1px solid var(--border);
  background: var(--card);
  color: var(--text-soft);
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.15s ease;
}
.fp-icon-btn:hover { color: var(--fit-brand); border-color: var(--fit-brand); }

.fp-user {
  display: flex; align-items: center; gap: 8px;
  padding: 4px 8px 4px 4px;
  border-radius: 999px;
  border: 1px solid var(--border);
  cursor: pointer;
  background: var(--card);
}
.fp-user:hover { border-color: var(--fit-brand); }
.fp-user__avatar {
  width: 28px; height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #7c5cff, #22d3ee);
  color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700;
}
.fp-user__name { font-size: 13px; color: var(--text); }

.fp-content {
  flex: 1;
  padding: 20px 24px 32px;
  overflow: auto;
}

/* 响应式：窄屏折叠侧栏 */
@media (max-width: 900px) {
  .fp-sidebar { width: 64px; flex-basis: 64px; }
  .fp-brand__name, .fp-nav__group, .fp-nav__item span, .fp-nav__item small { display: none; }
  .fp-nav__item { justify-content: center; }
}
</style>
