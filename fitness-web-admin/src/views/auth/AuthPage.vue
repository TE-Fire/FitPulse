<template>
  <div class="auth-wrap">
    <!-- 卡片：入场淡入上浮 -->
    <div class="auth-card animate-fade-up p-8 sm:p-10">

      <!-- Logo -->
      <div class="flex items-center gap-3 mb-8">
        <div class="w-11 h-11 rounded-xl bg-gradient-to-br from-pulse to-pulse-cyan flex items-center justify-center shadow-soft">
          <!-- 心电图标（Pulse 品牌意） -->
          <svg class="w-6 h-6 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M3 12h3l2-7 4 14 2-7h3l2 4h2" />
          </svg>
        </div>
        <div class="text-xl font-bold tracking-tight text-slate-800">
          Fit<span class="text-pulse">Pulse</span>
        </div>
      </div>

      <!-- 标题 + 副文案 -->
      <h1 class="text-2xl font-bold text-slate-800 mb-1">{{ isLogin ? '欢迎回来' : '创建账号' }}</h1>
      <p class="text-sm text-slate-500 mb-6">
        {{ isLogin ? '登录以继续你的健康训练旅程' : '加入 FitPulse，开启自律每一天' }}
      </p>

      <!-- 顶部 Tab：登录 / 注册 -->
      <div class="flex gap-8 border-b border-slate-200 mb-6">
        <button
          v-for="t in tabs"
          :key="t.key"
          @click="switchTab(t.key)"
          :class="[
            'pb-3 text-sm font-medium transition-colors relative -mb-px',
            activeTab === t.key
              ? 'text-pulse border-b-2 border-pulse'
              : 'text-slate-400 hover:text-slate-600 border-b-2 border-transparent'
          ]"
        >{{ t.label }}</button>
      </div>

      <!-- 登录表单 -->
      <form v-if="isLogin" @submit.prevent="onLogin" class="space-y-4">
        <!-- 登录方式内嵌 Tab：密码 / 验证码 -->
        <div class="flex gap-5 mb-1">
          <button
            v-for="m in loginModes"
            :key="m.key"
            type="button"
            @click="loginMode = m.key"
            :class="[
              'text-sm transition-colors',
              loginMode === m.key ? 'text-pulse font-medium' : 'text-slate-400 hover:text-slate-600'
            ]"
          >{{ m.label }}</button>
        </div>

        <!-- 邮箱 -->
        <div>
          <div class="relative">
            <el-icon :size="16" class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"><Message /></el-icon>
            <input
              v-model.trim="form.email"
              type="text"
              autocomplete="email"
              placeholder="QQ 邮箱（如 fire_dev@qq.com）"
              @blur="validateEmail"
              :class="inputClass('email')"
            >
          </div>
          <p v-if="errors.email" class="text-xs text-red-500 mt-1.5">{{ errors.email }}</p>
        </div>

        <!-- 密码登录 -->
        <div v-if="loginMode === 'password'">
          <div class="relative">
            <el-icon :size="16" class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"><Lock /></el-icon>
            <input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              autocomplete="current-password"
              placeholder="密码（8-64位，含字母+数字）"
              @blur="validatePassword"
              :class="inputClass('password') + ' pr-10'"
            >
            <button
              type="button"
              @click="showPassword = !showPassword"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
            >
              <el-icon :size="16"><View v-if="showPassword" /><Hide v-else /></el-icon>
            </button>
          </div>
          <p v-if="errors.password" class="text-xs text-red-500 mt-1.5">{{ errors.password }}</p>
        </div>

        <!-- 验证码登录 -->
        <div v-else>
          <div class="flex gap-2">
            <div class="relative flex-1">
              <el-icon :size="16" class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"><Key /></el-icon>
              <input
                v-model.trim="form.code"
                type="text"
                maxlength="6"
                inputmode="numeric"
                placeholder="6 位验证码"
                @blur="validateCode"
                :class="inputClass('code')"
              >
            </div>
            <button
              type="button"
              @click="sendLoginCode"
              :disabled="countdown > 0 || sendingCode"
              class="px-3 py-2.5 rounded-lg border border-slate-200 text-sm text-pulse hover:bg-pulse/5 transition disabled:opacity-50 disabled:cursor-not-allowed whitespace-nowrap"
            >
              {{ sendingCode ? '发送中…' : countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}
            </button>
          </div>
          <p v-if="errors.code" class="text-xs text-red-500 mt-1.5">{{ errors.code }}</p>
          <p v-if="devCode" class="text-xs text-slate-400 mt-1.5">开发联调验证码：{{ devCode }}</p>
        </div>

        <!-- 提交 -->
        <button
          type="submit"
          :disabled="submitting"
          class="w-full py-2.5 rounded-lg bg-gradient-to-r from-pulse to-pulse-cyan text-white font-medium shadow-soft hover:opacity-90 transition disabled:opacity-60 disabled:cursor-not-allowed"
        >
          {{ submitting ? '登录中…' : '登 录' }}
        </button>
      </form>

      <!-- 注册表单 -->
      <form v-else @submit.prevent="onRegister" class="space-y-4">
        <!-- 邮箱 -->
        <div>
          <div class="relative">
            <el-icon :size="16" class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"><Message /></el-icon>
            <input
              v-model.trim="form.email"
              type="text"
              autocomplete="email"
              placeholder="QQ 邮箱（如 fire_dev@qq.com）"
              @blur="validateEmail"
              :class="inputClass('email')"
            >
          </div>
          <p v-if="errors.email" class="text-xs text-red-500 mt-1.5">{{ errors.email }}</p>
        </div>

        <!-- 密码 -->
        <div>
          <div class="relative">
            <el-icon :size="16" class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"><Lock /></el-icon>
            <input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              autocomplete="new-password"
              placeholder="密码（8-64位，含字母+数字）"
              @blur="validatePassword"
              :class="inputClass('password') + ' pr-10'"
            >
            <button
              type="button"
              @click="showPassword = !showPassword"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
            >
              <el-icon :size="16"><View v-if="showPassword" /><Hide v-else /></el-icon>
            </button>
          </div>
          <p v-if="errors.password" class="text-xs text-red-500 mt-1.5">{{ errors.password }}</p>
        </div>

        <!-- 确认密码 -->
        <div>
          <div class="relative">
            <el-icon :size="16" class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"><Lock /></el-icon>
            <input
              v-model="form.confirmPassword"
              :type="showConfirm ? 'text' : 'password'"
              autocomplete="new-password"
              placeholder="再次输入密码"
              @blur="validateConfirm"
              :class="inputClass('confirmPassword') + ' pr-10'"
            >
            <button
              type="button"
              @click="showConfirm = !showConfirm"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
            >
              <el-icon :size="16"><View v-if="showConfirm" /><Hide v-else /></el-icon>
            </button>
          </div>
          <p v-if="errors.confirmPassword" class="text-xs text-red-500 mt-1.5">{{ errors.confirmPassword }}</p>
        </div>

        <!-- 验证码 -->
        <div>
          <div class="flex gap-2">
            <div class="relative flex-1">
              <el-icon :size="16" class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"><Key /></el-icon>
              <input
                v-model.trim="form.code"
                type="text"
                maxlength="6"
                inputmode="numeric"
                placeholder="6 位验证码"
                @blur="validateCode"
                :class="inputClass('code')"
              >
            </div>
            <button
              type="button"
              @click="sendRegisterCode"
              :disabled="countdown > 0 || sendingCode"
              class="px-3 py-2.5 rounded-lg border border-slate-200 text-sm text-pulse hover:bg-pulse/5 transition disabled:opacity-50 disabled:cursor-not-allowed whitespace-nowrap"
            >
              {{ sendingCode ? '发送中…' : countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}
            </button>
          </div>
          <p v-if="errors.code" class="text-xs text-red-500 mt-1.5">{{ errors.code }}</p>
          <p v-if="devCode" class="text-xs text-slate-400 mt-1.5">开发联调验证码：{{ devCode }}</p>
        </div>

        <!-- 提交 -->
        <button
          type="submit"
          :disabled="submitting"
          class="w-full py-2.5 rounded-lg bg-gradient-to-r from-pulse to-pulse-cyan text-white font-medium shadow-soft hover:opacity-90 transition disabled:opacity-60 disabled:cursor-not-allowed"
        >
          {{ submitting ? '注册中…' : '注 册' }}
        </button>
      </form>

      <!-- 底部切换 -->
      <p class="text-center text-sm text-slate-500 mt-6">
        <span v-if="isLogin">还没账号？</span>
        <span v-else>已有账号？</span>
        <button
          @click="switchTab(isLogin ? 'register' : 'login')"
          class="text-pulse font-medium hover:underline ml-1"
        >{{ isLogin ? '立即注册' : '去登录' }}</button>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  login as apiLogin,
  register as apiRegister,
  registerSendCode,
  loginSendCode
} from '@/api/auth'

const props = defineProps({ mode: { type: String, default: 'login' } })
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 顶部 Tab 由路由名驱动（/login → login, /register → register），URL 即状态
const activeTab = computed(() => (route.name === 'Register' ? 'register' : 'login'))
const isLogin = computed(() => activeTab.value === 'login')

const tabs = [
  { key: 'login', label: '登录' },
  { key: 'register', label: '注册' }
]
const loginModes = [
  { key: 'password', label: '密码登录' },
  { key: 'code', label: '验证码登录' }
]

// 登录方式：password / code
const loginMode = ref('password')

// 表单
const form = reactive({
  email: '',
  password: '',
  confirmPassword: '',
  code: ''
})
const errors = reactive({ email: '', password: '', confirmPassword: '', code: '' })
const showPassword = ref(false)
const showConfirm = ref(false)
const submitting = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
const devCode = ref('') // 开发联调明文验证码
let cdTimer = null

// 统一 input 样式
function inputClass(field) {
  const base = 'w-full pl-10 pr-3 py-2.5 rounded-lg border bg-white text-sm text-slate-800 placeholder-slate-400 outline-none transition'
  return errors[field]
    ? `${base} border-red-300 focus:border-red-400 focus:ring-2 focus:ring-red-500/10`
    : `${base} border-slate-200 focus:border-pulse focus:ring-2 focus:ring-pulse/20`
}

// 切换 Tab → 同步路由
function switchTab(key) {
  router.replace({ name: key === 'register' ? 'Register' : 'Login' })
  // 切换时清空错误与已发码
  Object.keys(errors).forEach(k => (errors[k] = ''))
  devCode.value = ''
}

// ---------- 校验 ----------
function validateEmail() {
  const v = form.email
  if (!v) { errors.email = '请输入邮箱'; return false }
  if (!/^[A-Za-z0-9._%+-]+@qq\.com$/.test(v)) {
    errors.email = '必须为合法的 @qq.com 邮箱'; return false
  }
  errors.email = ''
  return true
}
function validatePassword() {
  const v = form.password
  if (!v) { errors.password = '请输入密码'; return false }
  if (v.length < 8 || v.length > 64) { errors.password = '密码长度 8-64 位'; return false }
  if (!/[A-Za-z]/.test(v) || !/\d/.test(v)) { errors.password = '密码需同时包含字母和数字'; return false }
  errors.password = ''
  return true
}
function validateConfirm() {
  if (!form.confirmPassword) { errors.confirmPassword = '请再次输入密码'; return false }
  if (form.confirmPassword !== form.password) { errors.confirmPassword = '两次密码不一致'; return false }
  errors.confirmPassword = ''
  return true
}
function validateCode() {
  if (!form.code) { errors.code = '请输入验证码'; return false }
  if (!/^\d{6}$/.test(form.code)) { errors.code = '验证码为 6 位数字'; return false }
  errors.code = ''
  return true
}

// ---------- 发码 ----------
function startCountdown(sec) {
  countdown.value = sec
  if (cdTimer) clearInterval(cdTimer)
  cdTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) { clearInterval(cdTimer); cdTimer = null }
  }, 1000)
}

async function sendLoginCode() {
  if (!validateEmail()) return
  sendingCode.value = true
  try {
    const data = await loginSendCode(form.email)
    devCode.value = data.code
    ElMessage.success(`验证码已发送（开发明文：${data.code}）`)
    startCountdown(data.rateLimitSeconds || 60)
  } catch (_) { /* request 拦截器已 toast */ }
  finally { sendingCode.value = false }
}

async function sendRegisterCode() {
  if (!validateEmail()) return
  sendingCode.value = true
  try {
    const data = await registerSendCode(form.email)
    devCode.value = data.code
    ElMessage.success(`验证码已发送（开发明文：${data.code}）`)
    startCountdown(data.rateLimitSeconds || 60)
  } catch (_) { /* request 拦截器已 toast */ }
  finally { sendingCode.value = false }
}

// ---------- 提交 ----------
async function onLogin() {
  const okEmail = validateEmail()
  let okMain = true
  if (loginMode.value === 'password') okMain = validatePassword()
  else okMain = validateCode()
  if (!okEmail || !okMain) return

  submitting.value = true
  try {
    const payload = loginMode.value === 'password'
      ? { email: form.email, type: 1, password: form.password }
      : { email: form.email, type: 2, code: form.code }
    await userStore.login(payload)
    ElMessage.success('登录成功')
    router.replace(route.query.redirect ? String(route.query.redirect) : '/home')
  } catch (_) { /* 拦截器已 toast */ }
  finally { submitting.value = false }
}

async function onRegister() {
  const ok = [
    validateEmail(),
    validatePassword(),
    validateConfirm(),
    validateCode()
  ].every(Boolean)
  if (!ok) return

  submitting.value = true
  try {
    await apiRegister({
      email: form.email,
      password: form.password,
      code: form.code
    })
    ElMessage.success('注册成功，请登录')
    // 保留邮箱，清密码与验证码
    form.password = ''
    form.confirmPassword = ''
    form.code = ''
    devCode.value = ''
    switchTab('login')
  } catch (_) { /* 拦截器已 toast */ }
  finally { submitting.value = false }
}
</script>
