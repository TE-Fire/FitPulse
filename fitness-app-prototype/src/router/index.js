import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 路由表(对齐设计契约 4.2 Android 客户端导航)
// login/forgot 在 BottomNav 外,home/health/ai/profile 共用 Layout 含 BottomNav,且需要登录
const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue'), meta: { guestOnly: true } },
  { path: '/forgot-password', name: 'ForgotPassword', component: () => import('@/views/ForgotPassword.vue'), meta: { guestOnly: true } },
  {
    path: '/',
    component: () => import('@/components/Layout.vue'),
    children: [
      { path: 'home', name: 'Home', component: () => import('@/views/Home.vue'), meta: { tab: 'home', requiresAuth: true } },
      { path: 'health', name: 'Health', component: () => import('@/views/Health.vue'), meta: { tab: 'health', requiresAuth: true } },
      { path: 'ai', name: 'Ai', component: () => import('@/views/Ai.vue'), meta: { tab: 'ai', requiresAuth: true } },
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue'), meta: { tab: 'profile', requiresAuth: true } }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 全局路由守卫:未登录访问受保护页面 → 跳登录并携带 redirect;已登录访问 guestOnly 页面 → 跳 home
router.beforeEach((to) => {
  const userStore = useUserStore()
  if (to.meta?.requiresAuth && !userStore.isLoggedIn) {
    const redirect = to.fullPath && to.fullPath !== '/login' ? to.fullPath : undefined
    return redirect ? { path: '/login', query: { redirect } } : { path: '/login' }
  }
  if (to.meta?.guestOnly && userStore.isLoggedIn) {
    return { path: '/home' }
  }
  return true
})

export default router
