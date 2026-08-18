<template>
  <div class="page forgot-page">
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
      <h1 class="title">找回密码</h1>
      <p class="subtitle">通过邮箱验证码重置你的登录密码</p>
    </div>

    <!-- 表单卡片 -->
    <div class="form-card animate-fade-up">
      <!-- 邮箱 -->
      <form class="form-body" @submit.prevent="onReset">
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

        <!-- 验证码 + 获取按钮 -->
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
            @click="sendCode"
            :disabled="countdown > 0 || sendingCode"
            class="code-btn"
          >{{ sendingCode ? '发送中…' : countdown > 0 ? `${countdown}s` : '获取验证码' }}</button>
        </div>

        <p v-if="devCode" class="dev-code-tip">开发联调验证码:{{ devCode }}</p>

        <!-- 新密码 -->
        <div class="field">
          <div class="input-wrap">
            <input
              v-model="form.newPassword"
              :type="showNew ? 'text' : 'password'"
              autocomplete="new-password"
              placeholder="新密码(8-64位,含字母+数字)"
              class="input"
            >
            <button
              type="button"
              @click="showNew = !showNew"
              class="toggle-btn"
            >{{ showNew ? '隐藏' : '显示' }}</button>
          </div>
          <p v-if="errors.newPassword" class="err">{{ errors.newPassword }}</p>
        </div>

        <!-- 确认密码 -->
        <div class="field">
          <div class="input-wrap">
            <input
              v-model="form.confirmPassword"
              :type="showConfirm ? 'text' : 'password'"
              autocomplete="new-password"
              placeholder="再次输入新密码"
              class="input"
            >
            <button
              type="button"
              @click="showConfirm = !showConfirm"
              class="toggle-btn"
            >{{ showConfirm ? '隐藏' : '显示' }}</button>
          </div>
          <p v-if="errors.confirmPassword" class="err">{{ errors.confirmPassword }}</p>
        </div>

        <p v-if="successMsg" class="success-tip">{{ successMsg }}</p>

        <button type="submit" :disabled="submitting" class="primary-btn">
          {{ submitting ? '重置中…' : '重置密码' }}
        </button>
      </form>

      <!-- 底部:返回登录 -->
      <p class="switch-text">
        <span>想起密码?</span>
        <button @click="goLogin" class="switch-btn">返回登录</button>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { forgotPasswordSendCode, forgotPasswordReset } from '@/api/auth'

const router = useRouter()

const form = reactive({ email: '', code: '', newPassword: '', confirmPassword: '' })
const errors = reactive({ email: '', code: '', newPassword: '', confirmPassword: '' })
const showNew = ref(false)
const showConfirm = ref(false)
const submitting = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
const devCode = ref('')
const successMsg = ref('')
let cdTimer = null

function validateEmail() {
  const v = form.email
  if (!v) { errors.email = '请输入邮箱'; return false }
  if (!/^[A-Za-z0-9._%+-]+@qq\.com$/.test(v)) {
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
function validateNewPassword() {
  const v = form.newPassword
  if (!v) { errors.newPassword = '请输入新密码'; return false }
  if (v.length < 8 || v.length > 64) { errors.newPassword = '密码长度 8-64 位'; return false }
  if (!/[A-Za-z]/.test(v) || !/\d/.test(v)) { errors.newPassword = '密码需同时包含字母和数字'; return false }
  errors.newPassword = ''
  return true
}
function validateConfirm() {
  if (!form.confirmPassword) { errors.confirmPassword = '请再次输入密码'; return false }
  if (form.confirmPassword !== form.newPassword) { errors.confirmPassword = '两次密码不一致'; return false }
  errors.confirmPassword = ''
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

async function sendCode() {
  if (!validateEmail()) return
  sendingCode.value = true
  devCode.value = ''
  successMsg.value = ''
  try {
    const data = await forgotPasswordSendCode(form.email)
    devCode.value = data.code
    startCountdown(data.rateLimitSeconds || 60)
  } catch (e) {
    errors.code = e.message || '验证码发送失败,请稍后重试'
  } finally {
    sendingCode.value = false
  }
}

async function onReset() {
  const ok = [
    validateEmail(),
    validateCode(),
    validateNewPassword(),
    validateConfirm()
  ].every(Boolean)
  if (!ok) return

  submitting.value = true
  successMsg.value = ''
  try {
    await forgotPasswordReset({
      email: form.email,
      code: form.code,
      newPassword: form.newPassword,
      confirmPassword: form.confirmPassword
    })
    successMsg.value = '密码已重置,正在跳转登录页…'
    // 后端不自动登录,前端跳转登录页
    setTimeout(() => router.replace('/login'), 1000)
  } catch (e) {
    errors.code = e.message || '重置失败'
  } finally {
    submitting.value = false
  }
}

function goLogin() {
  router.replace('/login')
}
</script>

<style scoped>
/* 与 Login.vue 视觉风格 1:1 对齐 */
.forgot-page {
  padding: 56px 28px 32px 28px;
  background: linear-gradient(180deg, #f6f7fb 0%, #ffffff 60%);
  min-height: 100vh;
}

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

.form-card {
  background: white;
  border-radius: 20px;
  padding: 24px 20px;
  box-shadow: 0 12px 32px -8px rgba(30, 27, 75, 0.08);
}

.form-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.field {
  display: flex;
  flex-direction: column;
}
.code-field {
  flex-direction: row;
  gap: 8px;
}

.input-wrap {
  position: relative;
  display: flex;
  align-items: center;
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
.input-wrap .input {
  padding-right: 56px;
}
.input:focus {
  border-color: #7c5cff;
  background: white;
  box-shadow: 0 0 0 3px rgba(124, 92, 255, 0.12);
}
.input::placeholder {
  color: #94a3b8;
}

.toggle-btn {
  position: absolute;
  right: 10px;
  background: transparent;
  border: none;
  font-size: 12px;
  color: #94a3b8;
  cursor: pointer;
  padding: 2px 6px;
}
.toggle-btn:hover {
  color: #7c5cff;
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

.success-tip {
  font-size: 13px;
  color: #10b981;
  background: #ecfdf5;
  padding: 10px 12px;
  border-radius: 8px;
  margin: 0;
  text-align: center;
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
</style>
