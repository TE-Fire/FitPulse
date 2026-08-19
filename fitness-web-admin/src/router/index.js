import { createRouter, createWebHashHistory } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/stores/user'

NProgress.configure({ showSpinner: false })

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/AuthPage.vue'),
    props: { mode: 'login' },
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/AuthPage.vue'),
    props: { mode: 'register' },
    meta: { title: '注册' }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/auth/ForgotPassword.vue'),
    meta: { title: '找回密码' }
  },

  // ===== Admin Shell（统一 Layout 包裹） =====
  {
    path: '/',
    component: () => import('@/layout/Layout.vue'),
    redirect: '/dashboard/training',
    meta: { requiresAuth: true },
    children: [
      // 看板
      {
        path: 'dashboard/training',
        name: 'DashboardTraining',
        component: () => import('@/views/dashboard/Training.vue'),
        meta: { title: '训练看板', group: '看板' }
      },
      {
        path: 'dashboard/health',
        name: 'DashboardHealth',
        component: () => import('@/views/dashboard/Health.vue'),
        meta: { title: '健康看板', group: '看板' }
      },
      // 个人中心
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/Profile.vue'),
        meta: { title: '个人中心', group: '管理' }
      }
    ]
  },

  { path: '/:pathMatch(.*)*', redirect: '/dashboard/training' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  NProgress.start()
  document.title = (to.meta.title ? to.meta.title + ' · ' : '') + 'FitPulse'
  const store = useUserStore()
  // 已登录用户访问登录/注册页 → 直接回工作台
  if ((to.name === 'Login' || to.name === 'Register') && store.isLogin) {
    return next({ path: '/dashboard/training' })
  }
  // 受保护路由未登录 → 跳登录并记 redirect
  if (to.meta.requiresAuth && !store.isLogin) {
    return next({ name: 'Login', query: { redirect: to.fullPath } })
  }
  next()
})
router.afterEach(() => NProgress.done())

export default router
