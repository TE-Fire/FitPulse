import request from '@/utils/request'
import { USE_MOCK } from '@/config'
import * as mock from '@/mock/dashboard'

/**
 * 看板模块接口（对齐设计契约 §6.2 /admin/dashboard）
 * - USE_MOCK=true（开发期）：走前端 mock
 * - USE_MOCK=false：走真实后端，前缀 /api/v1
 * 成功响应：Result<T> = { code:200, message, data, timestamp }
 */

/** GET /admin/dashboard/training —— TrainingOverview（B/C 重点） */
export function getTrainingOverview() {
  if (USE_MOCK) return mock.getTrainingOverview()
  return request.get('/api/v1/admin/dashboard/training')
}

/** GET /admin/dashboard/health —— HealthOverview（A/B 重点） */
export function getHealthOverview() {
  if (USE_MOCK) return mock.getHealthOverview()
  return request.get('/api/v1/admin/dashboard/health')
}
