import request from '@/utils/request'

export function aiGeneratePlan(body) { return request.post('/api/v1/ai/generate-plan', body) }
export function aiDietAdvice(body) { return request.post('/api/v1/ai/diet-advice', body) }
export function aiChat(message) { return request.post('/api/v1/ai/chat', { message }) }
