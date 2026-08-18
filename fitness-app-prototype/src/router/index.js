import { createRouter, createWebHashHistory } from 'vue-router'

// 路由表(对齐设计契约 4.2 Android 客户端导航)
// login 在 BottomNav 外,home/health/ai/profile 共用 Layout 含 BottomNav
const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  { path: '/forgot-password', name: 'ForgotPassword', component: () => import('@/views/ForgotPassword.vue') },
  {
    path: '/',
    component: () => import('@/components/Layout.vue'),
    children: [
      { path: 'home', name: 'Home', component: () => import('@/views/Home.vue'), meta: { tab: 'home' } },
      { path: 'health', name: 'Health', component: () => import('@/views/Health.vue'), meta: { tab: 'health' } },
      { path: 'ai', name: 'Ai', component: () => import('@/views/Ai.vue'), meta: { tab: 'ai' } },
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue'), meta: { tab: 'profile' } }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
