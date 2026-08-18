<template>
  <div class="page ai-page">
    <!-- 顶部 -->
    <header class="ai-header">
      <div class="ai-title-row">
        <div class="ai-logo">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 8V4H8" />
            <rect width="16" height="12" x="4" y="8" rx="2" />
            <path d="M2 14h2" />
            <path d="M20 14h2" />
            <path d="M15 13v2" />
            <path d="M9 13v2" />
          </svg>
        </div>
        <div>
          <h1 class="ai-title">AI 健身教练</h1>
          <p class="ai-sub">训练 · 营养 · 恢复 · 减脂</p>
        </div>
      </div>
    </header>

    <!-- 对话区 -->
    <div ref="messagesRef" class="messages">
      <!-- 欢迎气泡 -->
      <div class="msg ai">
        <div class="msg-avatar ai-avatar">AI</div>
        <div class="msg-bubble ai-bubble">
          你好!我是你的 AI 健身教练。可以问我训练计划、营养建议、动作要点、恢复建议等。例如:
          <div class="suggest-list">
            <button class="suggest-btn" @click="useSuggestion('我今天练腿感觉膝盖疼,要不要减量?')">膝盖疼该不该减量?</button>
            <button class="suggest-btn" @click="useSuggestion('帮我设计一份减脂计划')">设计减脂计划</button>
            <button class="suggest-btn" @click="useSuggestion('我体重 68.5kg,如何减到 65kg?')">如何减到 65kg?</button>
          </div>
        </div>
      </div>

      <!-- 历史消息 -->
      <div v-for="(msg, i) in messages" :key="i" :class="['msg', msg.role]">
        <div v-if="msg.role === 'ai'" class="msg-avatar ai-avatar">AI</div>
        <div class="msg-bubble" :class="msg.role + '-bubble'">{{ msg.content }}</div>
        <div v-if="msg.role === 'user'" class="msg-avatar user-avatar">我</div>
      </div>

      <!-- 思考中 -->
      <div v-if="thinking" class="msg ai">
        <div class="msg-avatar ai-avatar">AI</div>
        <div class="msg-bubble ai-bubble thinking-bubble">
          <span class="dot"></span>
          <span class="dot"></span>
          <span class="dot"></span>
        </div>
      </div>
    </div>

    <!-- 输入区 -->
    <div class="input-area">
      <input
        v-model="input"
        @keydown.enter="send"
        placeholder="输入你的问题..."
        :disabled="thinking"
        class="input"
      >
      <button @click="send" :disabled="thinking || !input.trim()" class="send-btn">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="m22 2-7 20-4-9-9-4Z" />
          <path d="M22 2 11 13" />
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { chat } from '@/api/ai'

const input = ref('')
const messages = ref([])
const thinking = ref(false)
const messagesRef = ref(null)
let conversationId = null

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

function useSuggestion(text) {
  input.value = text
  send()
}

async function send() {
  const text = input.value.trim()
  if (!text || thinking.value) return

  // 追加用户消息
  messages.value.push({ role: 'user', content: text })
  input.value = ''
  thinking.value = true
  scrollToBottom()

  try {
    const data = await chat({ message: text, conversationId })
    conversationId = data.conversationId
    messages.value.push({ role: 'ai', content: data.reply })
  } catch (e) {
    messages.value.push({ role: 'ai', content: '抱歉,出错了:' + e.message })
  } finally {
    thinking.value = false
    scrollToBottom()
  }
}
</script>

<style scoped>
.ai-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f6f7fb;
  padding-bottom: 0;
}

/* Header */
.ai-header {
  padding: 24px 16px 16px 16px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
}
.ai-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.ai-logo {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #7c5cff 0%, #22d3ee 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 16px -4px rgba(124, 92, 255, 0.4);
}
.ai-logo svg {
  width: 22px;
  height: 22px;
}
.ai-title {
  font-size: 17px;
  font-weight: 700;
  color: #0f172a;
  margin: 0;
}
.ai-sub {
  font-size: 12px;
  color: #64748b;
  margin: 2px 0 0 0;
}

/* 对话区 */
.messages {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  padding: 16px 12px 16px 12px;
  padding-bottom: 130px;
}

/* 消息 */
.msg {
  display: flex;
  margin-bottom: 14px;
  align-items: flex-end;
  gap: 8px;
}
.msg.user {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 28px;
  height: 28px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 700;
  flex-shrink: 0;
}
.ai-avatar {
  background: linear-gradient(135deg, #7c5cff 0%, #22d3ee 100%);
  color: white;
}
.user-avatar {
  background: #1e293b;
  color: white;
}

.msg-bubble {
  max-width: 76%;
  padding: 10px 14px;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
.ai-bubble {
  background: white;
  color: #1e293b;
  border-radius: 4px 16px 16px 16px;
  box-shadow: 0 2px 8px -2px rgba(30, 27, 75, 0.08);
}
.user-bubble {
  background: linear-gradient(135deg, #7c5cff 0%, #6d28e9 100%);
  color: white;
  border-radius: 16px 4px 16px 16px;
}

.suggest-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 8px;
}
.suggest-btn {
  text-align: left;
  padding: 8px 10px;
  font-size: 12px;
  color: #7c5cff;
  background: rgba(124, 92, 255, 0.08);
  border: 1px solid rgba(124, 92, 255, 0.2);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s ease;
}
.suggest-btn:hover {
  background: rgba(124, 92, 255, 0.12);
}

/* 思考中 */
.thinking-bubble {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 14px 16px;
}
.dot {
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: #94a3b8;
  animation: dot-bounce 1.2s ease-in-out infinite;
}
.dot:nth-child(2) {
  animation-delay: 0.2s;
}
.dot:nth-child(3) {
  animation-delay: 0.4s;
}
@keyframes dot-bounce {
  0%, 80%, 100% { transform: translateY(0); opacity: 0.5; }
  40% { transform: translateY(-4px); opacity: 1; }
}

/* 输入区:固定在 BottomNav 上方 */
.input-area {
  position: fixed;
  bottom: 64px;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 420px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-top: 1px solid #e2e8f0;
  display: flex;
  gap: 8px;
  align-items: center;
  z-index: 40;
  padding-bottom: calc(8px + env(safe-area-inset-bottom, 0px));
}
@media (min-width: 768px) {
  .input-area {
    max-width: 390px;
    bottom: calc(4vh + 64px);
  }
}

.input {
  flex: 1;
  padding: 10px 14px;
  font-size: 14px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 999px;
  outline: none;
  color: #1e293b;
  font-family: inherit;
  transition: all 0.15s ease;
}
.input:focus {
  border-color: #7c5cff;
  background: white;
  box-shadow: 0 0 0 3px rgba(124, 92, 255, 0.12);
}

.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 999px;
  background: linear-gradient(135deg, #7c5cff 0%, #22d3ee 100%);
  color: white;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  transition: all 0.15s ease;
  box-shadow: 0 4px 12px -2px rgba(124, 92, 255, 0.4);
}
.send-btn svg {
  width: 18px;
  height: 18px;
}
.send-btn:active {
  transform: scale(0.94);
}
.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
