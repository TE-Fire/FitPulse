<template>
  <div class="page login-page">
    <!-- 顶部品牌区 -->
    <div class="brand-area animate-fade-up">
      <div class="logo-wrap">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M3 12h3l2-7 4 14 2-7h3l2 4h2" />
          </svg>
        </div>
        <div class="brand-text">Fit<span class="brand-accent">Pulse</span></div>
      </div>
      <h1 class="title">{{ isLogin ? '欢迎回来' : '创建账号' }}</h1>
      <p class="subtitle">{{ isLogin ? '登录以继续你的健康训练旅程' : '加入 FitPulse,开启自律每一天' }}</p>
    </div>

    <!-- 表单卡片 -->
    <div class="form-card animate-fade-up">
      <!-- 顶部 Tab:登录 / 注册 -->
      <div class="tabs">
        <button
          v-for="t in tabs"
          :key="t.key"
          @click="switchTab(t.key)"
          :class="['tab-btn', activeTab === t.key && 'tab-btn-active']"
        >{{ t.label }}</button>
      </div>

      <!-- 登录表单 -->
      <form v-if="isLogin" @submit.prevent="onLogin" class="form-body">
        <div class="login-modes">
          <button
            v-for="m in loginModes"
            :key="m.key"
            type="button"
            @click="loginMode = m.key"
            :class="['mode-btn', loginMode === m.key && 'mode-btn-active']"
          >{{ m.label }}</button>
        </div>

        <div class="field">
          <input
            v-model.trim="form.email"
            type="text"
            autocomplete="email"
            placeholder="QQ 邮箱(如 fire_dev@qq.com)"
            class="input"
          >
          <p v-if="errors.email" class="err">{{ errors.email }}</p>
        </div>

        <div v-if="loginMode === 'password'" class="field">
          <input
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            autocomplete="current-password"
            placeholder="密码(8-64位,含字母+数字)"
            class="input"
          >
          <p v-if="errors.password" class="err">{{ errors.password }}</p>
        </div>

        <div v-if="loginMode === 'password'" class="forgot-row">
          <router-link to="/forgot-password" class="forgot-link">忘记密码?</router-link>
        </div>

        <div v-else class="field code-field">
          <input
            v-model.trim="form.code"
            type="text"
            maxlength="6"
            inputmode="numeric"
            placeholder="6 位验证码"
            class="input"
          >
          <button
            type="button"
            @click="sendLoginCode"
            :disabled="countdown > 0 || sendingCode"
            class="code-btn"
          >{{ sendingCode ? '发送中…' : countdown > 0 ? `${countdown}s` : '获取验证码' }}</button>
        </div>

        <p v-if="devCode" class="dev-code-tip">开发联调验证码:{{ devCode }}</p>

        <button type="submit" :disabled="submitting" class="primary-btn">
          {{ submitting ? '登录中…' : '登 录' }}
        </button>
      </form>

      <!-- 注册表单 -->
      <form v-else @submit.prevent="onRegister" class="form-body">
        <div class="field">
          <input
            v-model.trim="form.email"
            type="text"
            autocomplete="email"
            placeholder="QQ 邮箱(如 fire_dev@qq.com)"
            class="input"
          >
          <p v-if="errors.email" class="err">{{ errors.email }}</p>
        </div>

        <div class="field">
          <input
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            autocomplete="new-password"
            placeholder="密码(8-64位,含字母+数字)"
            class="input"
          >
          <p v-if="errors.password" class="err">{{ errors.password }}</p>
        </div>

        <div class="field">
          <input
            v-model="form.confirmPassword"
            :type="showConfirm ? 'text' : 'password'"
            autocomplete="new-password"
            placeholder="再次输入密码"
            class="input"
          >
          <p v-if="errors.confirmPassword" class="err">{{ errors.confirmPassword }}</p>
        </div>

        <div class="field code-field">
          <input
            v-model.trim="form.code"
            type="text"
            maxlength="6"
            inputmode="numeric"
            placeholder="6 位验证码"
            class="input"
          >
          <button
            type="button"
            @click="sendRegisterCode"
            :disabled="countdown > 0 || sendingCode"
            class="code-btn"
          >{{ sendingCode ? '发送中…' : countdown > 0 ? `${countdown}s` : '获取验证码' }}</button>
        </div>

        <p v-if="devCode" class="dev-code-tip">开发联调验证码:{{ devCode }}</p>

        <button type="submit" :disabled="submitting" class="primary-btn">
          {{ submitting ? '注册中…' : '注 册' }}
        </button>
      </form>

      <!-- 底部切换 -->
      <p class="switch-text">
        <span v-if="isLogin">还没账号?</span>
        <span v-else>已有账号?</span>
        <button @click="switchTab(isLogin ? 'register' : 'login')" class="switch-btn">
          {{ isLogin ? '立即注册' : '去登录' }}
        </button>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login as apiLogin, register as apiRegister, loginSendCode, registerSendCode } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('login')
const isLogin = computed(() => activeTab.value === 'login')
const tabs = [
  { key: 'login', label: '登录' },
  { key: 'register', label: '注册' }
]
const loginModes = [
  { key: 'password', label: '密码登录' },
  { key: 'code', label: '验证码登录' }
]
const loginMode = ref('password')

const form = reactive({ email: '', password: '', confirmPassword: '', code: '' })
const errors = reactive({ email: '', password: '', confirmPassword: '', code: '' })
const showPassword = ref(false)
const showConfirm = ref(false)
const submitting = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
const devCode = ref('')
let cdTimer = null

function switchTab(key) {
  activeTab.value = key
  Object.keys(errors).forEach(k => (errors[k] = ''))
  devCode.value = ''
}

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
    startCountdown(data.rateLimitSeconds || 60)
  } catch (_) {
    errors.code = '验证码发送失败,请稍后重试'
  } finally {
    sendingCode.value = false
  }
}

async function sendRegisterCode() {
  if (!validateEmail()) return
  sendingCode.value = true
  try {
    const data = await registerSendCode(form.email)
    devCode.value = data.code
    startCountdown(data.rateLimitSeconds || 60)
  } catch (_) {
    errors.code = '验证码发送失败,请稍后重试'
  } finally {
    sendingCode.value = false
  }
}

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
    router.replace('/home')
  } catch (e) {
    errors.password = e.message || '登录失败'
  } finally {
    submitting.value = false
  }
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
    form.password = ''
    form.confirmPassword = ''
    form.code = ''
    devCode.value = ''
    switchTab('login')
  } catch (e) {
    errors.code = e.message || '注册失败'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.login-page {
  padding: 56px 28px 32px 28px;
  background: linear-gradient(180deg, #f6f7fb 0%, #ffffff 60%);
  min-height: 100vh;
}

/* 顶部品牌区 */
.brand-area {
  text-align: center;
  margin-bottom: 32px;
}
.logo-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 20px;
}
.logo-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, #7c5cff 0%, #22d3ee 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 20px -6px rgba(124, 92, 255, 0.4);
}
.logo-icon svg {
  width: 24px;
  height: 24px;
}
.brand-text {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: -0.02em;
}
.brand-accent {
  color: #7c5cff;
}
.title {
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 8px 0;
}
.subtitle {
  font-size: 13px;
  color: #64748b;
  margin: 0;
}

/* 表单卡片 */
.form-card {
  background: white;
  border-radius: 20px;
  padding: 24px 20px;
  box-shadow: 0 12px 32px -8px rgba(30, 27, 75, 0.08);
}

/* Tab */
.tabs {
  display: flex;
  gap: 24px;
  border-bottom: 1px solid #e2e8f0;
  margin-bottom: 20px;
}
.tab-btn {
  padding: 8px 0;
  font-size: 14px;
  font-weight: 500;
  color: #94a3b8;
  background: transparent;
  border: none;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  cursor: pointer;
  transition: all 0.15s ease;
}
.tab-btn-active {
  color: #7c5cff;
  border-bottom-color: #7c5cff;
}

/* 表单 */
.form-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.login-modes {
  display: flex;
  gap: 20px;
}
.mode-btn {
  font-size: 13px;
  font-weight: 500;
  color: #94a3b8;
  background: transparent;
  border: none;
  padding: 0;
  cursor: pointer;
  transition: color 0.15s ease;
}
.mode-btn-active {
  color: #7c5cff;
}

.field {
  display: flex;
  flex-direction: column;
}
.code-field {
  flex-direction: row;
  gap: 8px;
}
.input {
  flex: 1;
  width: 100%;
  padding: 12px 14px;
  font-size: 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  outline: none;
  color: #1e293b;
  transition: all 0.15s ease;
  font-family: inherit;
}
.input:focus {
  border-color: #7c5cff;
  background: white;
  box-shadow: 0 0 0 3px rgba(124, 92, 255, 0.12);
}
.input::placeholder {
  color: #94a3b8;
}

.code-btn {
  padding: 12px 16px;
  font-size: 13px;
  font-weight: 500;
  color: #7c5cff;
  background: #f3f0ff;
  border: 1px solid #ddd6fe;
  border-radius: 10px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.15s ease;
}
.code-btn:disabled {
  color: #94a3b8;
  background: #f1f5f9;
  border-color: #e2e8f0;
  cursor: not-allowed;
}

.err {
  font-size: 12px;
  color: #ef4444;
  margin: 6px 2px 0 2px;
}

.dev-code-tip {
  font-size: 12px;
  color: #64748b;
  background: #f1f5f9;
  padding: 6px 10px;
  border-radius: 6px;
  margin: 0;
}

.primary-btn {
  margin-top: 4px;
  padding: 14px;
  font-size: 15px;
  font-weight: 600;
  color: white;
  background: linear-gradient(135deg, #7c5cff 0%, #22d3ee 100%);
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.15s ease;
  box-shadow: 0 8px 20px -6px rgba(124, 92, 255, 0.4);
}
.primary-btn:active {
  transform: translateY(1px);
}
.primary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.switch-text {
  text-align: center;
  font-size: 13px;
  color: #64748b;
  margin: 16px 0 0 0;
}
.switch-btn {
  background: transparent;
  border: none;
  color: #7c5cff;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  margin-left: 4px;
  padding: 0;
}

.forgot-row {
  display: flex;
  justify-content: flex-end;
  margin: -6px 2px 0 0;
}
.forgot-link {
  font-size: 12px;
  color: #7c5cff;
  font-weight: 500;
  text-decoration: none;
  transition: color 0.15s ease;
}
.forgot-link:hover {
  color: #6d28d9;
}
</style>
