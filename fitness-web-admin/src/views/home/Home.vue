<template>
  <div class="auth-wrap">
    <div class="auth-card animate-fade-up p-10 text-center">
      <!-- Logo -->
      <div class="flex items-center justify-center gap-3 mb-6">
        <div class="w-11 h-11 rounded-xl bg-gradient-to-br from-pulse to-pulse-cyan flex items-center justify-center shadow-soft">
          <svg class="w-6 h-6 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M3 12h3l2-7 4 14 2-7h3l2 4h2" />
          </svg>
        </div>
        <div class="text-xl font-bold tracking-tight text-slate-800">
          Fit<span class="text-pulse">Pulse</span>
        </div>
      </div>

      <h1 class="text-2xl font-bold text-slate-800 mb-2">登录成功</h1>
      <p class="text-sm text-slate-500 mb-1">欢迎回来，</p>
      <p class="text-lg font-medium text-pulse mb-8">{{ username }}</p>

      <!-- 凭证信息（仅用于验证 auth 链路，后续接入业务模块时移除） -->
      <div class="bg-slate-50 border border-slate-200 rounded-lg p-4 text-left mb-6 space-y-1.5">
        <div class="text-xs text-slate-400">userId</div>
        <div class="text-sm font-mono text-slate-700 break-all">{{ userId }}</div>
      </div>

      <button
        @click="onLogout"
        :disabled="logging"
        class="w-full py-2.5 rounded-lg bg-gradient-to-r from-pulse to-pulse-cyan text-white font-medium shadow-soft hover:opacity-90 transition disabled:opacity-60"
      >
        {{ logging ? '登出中…' : '退出登录' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const username = computed(() => userStore.username)
const userId = computed(() => userStore.userInfo?.userId ?? '-')
const logging = ref(false)

async function onLogout() {
  logging.value = true
  try {
    await userStore.logout()
    ElMessage.success('已退出登录')
    router.replace({ name: 'Login' })
  } catch (_) { /* 即使后端失败也已清本地 */ }
  finally { logging.value = false }
}
</script>
