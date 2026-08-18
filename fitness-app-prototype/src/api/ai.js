// AI 模块 API(对齐 docs/接口文档.md 七、AI)
import { mockAiChat } from '@/mock'
import { mockCall } from '@/utils/request'

// AI 对话 POST /ai/chat  body:{message, conversationId?}
export function chat(data) {
  return mockCall(mockAiChat, data)
}
