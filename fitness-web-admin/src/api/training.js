import request from '@/utils/request'
import * as mock from '@/mock/training'

/**
 * 训练模块接口（对齐后端 TrainingController 规范）
 * 当前阶段：强制走前端 Mock（USE_MOCK 开关被忽略，后端接口就绪后再对齐）
 * 接口前缀：/api/v1/training
 *   - exercises/*   动作库
 *   - plans/*       训练计划
 *   - records/*     训练记录
 */

// —— 后端接口就绪前，训练模块强制走 mock
// —— 如需切换真实后端：把下面 FORCE_MOCK 改成 false，并在 .env 设 VITE_USE_MOCK=false
const FORCE_MOCK = true

const useMock = FORCE_MOCK

/* ==================== 动作库 Exercise ==================== */

/** GET /training/exercises —— 分页查询动作列表（name/category/difficulty 筛选） */
export function getExerciseList(params) {
  if (useMock) return mock.getExerciseList(params)
  return request.get('/api/v1/training/exercises', { params })
}

/** GET /training/exercises/{id} —— 查询动作详情 */
export function getExerciseDetail(id) {
  if (useMock) return mock.getExerciseDetail(id)
  return request.get(`/api/v1/training/exercises/${id}`)
}

/** POST /training/exercises —— 新建动作 */
export function createExercise(data) {
  if (useMock) return mock.createExercise(data)
  return request.post('/api/v1/training/exercises', data)
}

/** PUT /training/exercises/{id} —— 修改动作 */
export function updateExercise(id, data) {
  if (useMock) return mock.updateExercise(id, data)
  return request.put(`/api/v1/training/exercises/${id}`, data)
}

/** DELETE /training/exercises/{id} —— 删除动作（系统动作不可删） */
export function deleteExercise(id) {
  if (useMock) return mock.deleteExercise(id)
  return request.delete(`/api/v1/training/exercises/${id}`)
}

/** 辅助：获取全部动作（用于计划编辑页选择器，不分页） */
export function getAllExercises() {
  if (useMock) return mock.getAllExercises()
  // 后端没有单独不分页接口时，复用列表接口拉 500 条
  return request.get('/api/v1/training/exercises', { params: { size: 500 } }).then(r => r.records || [])
}

/* ==================== 训练计划 WorkoutPlan ==================== */

/** GET /training/plans —— 当前用户的计划列表（分页） */
export function getPlanList(params) {
  if (useMock) return mock.getPlanList(params)
  return request.get('/api/v1/training/plans', { params })
}

/** GET /training/plans/{id} —— 计划详情（含关联动作 items） */
export function getPlanDetail(id) {
  if (useMock) return mock.getPlanDetail(id)
  return request.get(`/api/v1/training/plans/${id}`)
}

/** POST /training/plans —— 新建计划（同时提交 items） */
export function createPlan(data) {
  if (useMock) return mock.createPlan(data)
  return request.post('/api/v1/training/plans', data)
}

/** PUT /training/plans/{id} —— 修改计划（全量替换 items） */
export function updatePlan(id, data) {
  if (useMock) return mock.updatePlan(id, data)
  return request.put(`/api/v1/training/plans/${id}`, data)
}

/** DELETE /training/plans/{id} —— 删除计划 */
export function deletePlan(id) {
  if (useMock) return mock.deletePlan(id)
  return request.delete(`/api/v1/training/plans/${id}`)
}

/** 辅助：获取全部计划（用于记录录入页下拉选择，不分页） */
export function getAllPlans() {
  if (useMock) return mock.getAllPlans()
  return request.get('/api/v1/training/plans', { params: { size: 500 } }).then(r => r.records || [])
}

/* ==================== 训练记录 WorkoutRecord ==================== */

/** GET /training/records —— 训练记录列表（分页 + 日期范围筛选） */
export function getRecordList(params) {
  if (useMock) return mock.getRecordList(params)
  return request.get('/api/v1/training/records', { params })
}

/** GET /training/records/{id} —— 记录详情（含每组明细 sets） */
export function getRecordDetail(id) {
  if (useMock) return mock.getRecordDetail(id)
  return request.get(`/api/v1/training/records/${id}`)
}

/** POST /training/records —— 提交训练记录（含 sets，后端自动计算容量） */
export function createRecord(data) {
  if (useMock) return mock.createRecord(data)
  return request.post('/api/v1/training/records', data)
}

/* ==================== 枚举导出（UI 层复用） ==================== */
export {
  CATEGORY_OPTIONS,
  DIFFICULTY_OPTIONS,
  EQUIPMENT_OPTIONS
} from '@/mock/training'
