import { createRouter, createWebHashHistory } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/stores/user'

NProgress.configure({ showSpinner: false })

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/auth/Login.vue'), meta: { title: '登录' } },
  {
    path: '/',
    component: () => import('@/layout/AdminLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/Index.vue'), meta: { title: '数据看板' } },
      { path: 'content/exercises', name: 'ContentExercises', component: () => import('@/views/content/Exercises.vue'), meta: { title: '动作库' } },
      { path: 'content/plans', name: 'ContentPlans', component: () => import('@/views/content/Plans.vue'), meta: { title: '训练模板' } },
      { path: 'content/foods', name: 'ContentFoods', component: () => import('@/views/content/Foods.vue'), meta: { title: '食物库' } },
      { path: 'content/files', name: 'ContentFiles', component: () => import('@/views/content/Files.vue'), meta: { title: '文件资源' } },
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue'), meta: { title: '个人中心' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  NProgress.start()
  document.title = (to.meta.title ? to.meta.title + ' · ' : '') + 'FitPulse'
  if (to.meta.requiresAuth) {
    const store = useUserStore()
    if (!store.token) { next({ name: 'Login', query: { redirect: to.fullPath } }) }
    else next()
  } else next()
})
router.afterEach(() => NProgress.done())

export default router
