// Dashboard 模块 API(对齐 docs/接口文档.md 三、Dashboard)
import { mockTrainingDashboard, mockHealthDashboard } from '@/mock'
import { mockCall } from '@/utils/request'

// 训练看板 GET /admin/dashboard/training
export function getTrainingDashboard() {
  return mockCall(mockTrainingDashboard)
}

// 健康看板 GET /admin/dashboard/health
export function getHealthDashboard() {
  return mockCall(mockHealthDashboard)
}
