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
    path: '/home',
    name: 'Home',
    component: () => import('@/views/home/Home.vue'),
    meta: { title: '首页', requiresAuth: true }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/auth/ForgotPassword.vue'),
    meta: { title: '找回密码' }
  },
  { path: '/', redirect: '/home' },
  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  NProgress.start()
  document.title = (to.meta.title ? to.meta.title + ' · ' : '') + 'FitPulse'
  const store = useUserStore()
  // 已登录用户访问登录/注册页 → 直接回首页
  if ((to.name === 'Login' || to.name === 'Register') && store.isLogin) {
    return next({ name: 'Home' })
  }
  // 受保护路由未登录 → 跳登录并记 redirect
  if (to.meta.requiresAuth && !store.isLogin) {
    return next({ name: 'Login', query: { redirect: to.fullPath } })
  }
  next()
})
router.afterEach(() => NProgress.done())

export default router
