<template>
  <div class="auth-wrap">
    <div class="auth-card animate-fade-up p-8 sm:p-10">

      <!-- Logo（与登录/注册页一致） -->
      <div class="flex items-center gap-3 mb-8">
        <div class="w-11 h-11 rounded-xl bg-gradient-to-br from-pulse to-pulse-cyan flex items-center justify-center shadow-soft">
          <svg class="w-6 h-6 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M3 12h3l2-7 4 14 2-7h3l2 4h2" />
          </svg>
        </div>
        <div class="text-xl font-bold tracking-tight text-slate-800">
          Fit<span class="text-pulse">Pulse</span>
        </div>
      </div>

      <h1 class="text-2xl font-bold text-slate-800 mb-1">找回密码</h1>
      <p class="text-sm text-slate-500 mb-6">输入注册邮箱，验证后设置新密码</p>

      <form @submit.prevent="onReset" class="space-y-4">
        <!-- 邮箱 + 发码 -->
        <div>
          <div class="flex gap-2">
            <div class="relative flex-1">
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
            <button
              type="button"
              @click="sendCode"
              :disabled="countdown > 0 || sendingCode"
              class="px-3 py-2.5 rounded-lg border border-slate-200 text-sm text-pulse hover:bg-pulse/5 transition disabled:opacity-50 disabled:cursor-not-allowed whitespace-nowrap"
            >
              {{ sendingCode ? '发送中…' : countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}
            </button>
          </div>
          <p v-if="errors.email" class="text-xs text-red-500 mt-1.5">{{ errors.email }}</p>
          <p v-if="devCode" class="text-xs text-slate-400 mt-1.5">开发联调验证码：{{ devCode }}</p>
        </div>

        <!-- 验证码 -->
        <div>
          <div class="relative">
            <el-icon :size="16" class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"><Key /></el-icon>
            <input
              v-model.trim="form.code"
              type="text"
              maxlength="6"
              inputmode="numeric"
              placeholder="6 位密码重置验证码"
              @blur="validateCode"
              :class="inputClass('code')"
            >
          </div>
          <p v-if="errors.code" class="text-xs text-red-500 mt-1.5">{{ errors.code }}</p>
        </div>

        <!-- 新密码 -->
        <div>
          <div class="relative">
            <el-icon :size="16" class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"><Lock /></el-icon>
            <input
              v-model="form.newPassword"
              :type="showNew ? 'text' : 'password'"
              autocomplete="new-password"
              placeholder="新密码（8-64位，含字母+数字）"
              @blur="validateNew"
              :class="inputClass('newPassword') + ' pr-10'"
            >
            <button
              type="button"
              @click="showNew = !showNew"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
            >
              <el-icon :size="16"><View v-if="showNew" /><Hide v-else /></el-icon>
            </button>
          </div>
          <p v-if="errors.newPassword" class="text-xs text-red-500 mt-1.5">{{ errors.newPassword }}</p>
        </div>

        <!-- 确认密码 -->
        <div>
          <div class="relative">
            <el-icon :size="16" class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"><Lock /></el-icon>
            <input
              v-model="form.confirmPassword"
              :type="showConfirm ? 'text' : 'password'"
              autocomplete="new-password"
              placeholder="再次输入新密码"
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

        <!-- 提交 -->
        <button
          type="submit"
          :disabled="submitting"
          class="w-full py-2.5 rounded-lg bg-gradient-to-r from-pulse to-pulse-cyan text-white font-medium shadow-soft hover:opacity-90 transition disabled:opacity-60 disabled:cursor-not-allowed"
        >
          {{ submitting ? '重置中…' : '重置密码' }}
        </button>
      </form>

      <!-- 返回登录 -->
      <p class="text-center text-sm text-slate-500 mt-6">
        想起密码了？
        <router-link to="/login" class="text-pulse font-medium hover:underline ml-1">返回登录</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { forgotPasswordSendCode, forgotPasswordReset } from '@/api/auth'

const router = useRouter()

const form = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})
const errors = reactive({ email: '', code: '', newPassword: '', confirmPassword: '' })
const showNew = ref(false)
const showConfirm = ref(false)
const submitting = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
const devCode = ref('')
let cdTimer = null

// 复用 AuthPage 的 input 样式
function inputClass(field) {
  const base = 'w-full pl-10 pr-3 py-2.5 rounded-lg border bg-white text-sm text-slate-800 placeholder-slate-400 outline-none transition'
  return errors[field]
    ? `${base} border-red-300 focus:border-red-400 focus:ring-2 focus:ring-red-500/10`
    : `${base} border-slate-200 focus:border-pulse focus:ring-2 focus:ring-pulse/20`
}

// ---------- 校验 ----------
function validateEmail() {
  if (!form.email) { errors.email = '请输入邮箱'; return false }
  if (!/^[A-Za-z0-9._%+-]+@qq\.com$/.test(form.email)) {
    errors.email = '必须为合法的 @qq.com 邮箱'; return false
  }
  errors.email = ''
  return true
}
function validateCode() {
  if (!form.code) { errors.code = '请输入验证码'; return false }
  if (!/^\d{6}$/.test(form.code)) { errors.code = '验证码为 6 位数字'; return false }
  errors.code = ''
  return true
}
function validateNew() {
  const v = form.newPassword
  if (!v) { errors.newPassword = '请输入新密码'; return false }
  if (v.length < 8 || v.length > 64) { errors.newPassword = '密码长度 8-64 位'; return false }
  if (!/[A-Za-z]/.test(v) || !/\d/.test(v)) { errors.newPassword = '密码需同时包含字母和数字'; return false }
  errors.newPassword = ''
  return true
}
function validateConfirm() {
  if (!form.confirmPassword) { errors.confirmPassword = '请再次输入新密码'; return false }
  if (form.confirmPassword !== form.newPassword) { errors.confirmPassword = '两次密码不一致'; return false }
  errors.confirmPassword = ''
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

async function sendCode() {
  if (!validateEmail()) return
  sendingCode.value = true
  try {
    const data = await forgotPasswordSendCode(form.email)
    devCode.value = data.code
    ElMessage.success(`验证码已发送（开发明文：${data.code}）`)
    startCountdown(data.rateLimitSeconds || 60)
  } catch (_) { /* request 拦截器已 toast，如 404 邮箱未注册 */ }
  finally { sendingCode.value = false }
}

// ---------- 提交 ----------
async function onReset() {
  const ok = [
    validateEmail(),
    validateCode(),
    validateNew(),
    validateConfirm()
  ].every(Boolean)
  if (!ok) return

  submitting.value = true
  try {
    await forgotPasswordReset({
      email: form.email,
      code: form.code,
      newPassword: form.newPassword,
      confirmPassword: form.confirmPassword
    })
    ElMessage.success('密码已重置，请使用新密码登录')
    router.replace({ name: 'Login', query: { email: form.email } })
  } catch (_) { /* 拦截器已 toast */ }
  finally { submitting.value = false }
}
</script>
