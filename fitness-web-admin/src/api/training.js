import request from '@/utils/request'

/**
 * 训练模块接口（对齐后端 TrainingController）
 * 接口前缀：/api/v1/training
 *   - exercises/*   动作库（管理端保留 CRUD）
 *   - plans/*       训练计划（管理端只读查看）
 *   - records/*     训练记录（管理端只读查看）
 *
 * 响应结构：request 拦截器已自动解包 Result.data
 */

/* ==================== 动作库 Exercise ==================== */

/** GET /training/exercises —— 分页查询动作列表 */
export function getExerciseList(params) {
  return request.get('/api/v1/training/exercises', { params })
}

/** GET /training/exercises/{id} —— 查询动作详情 */
export function getExerciseDetail(id) {
  return request.get(`/api/v1/training/exercises/${id}`)
}

/** POST /training/exercises —— 新建动作 */
export function createExercise(data) {
  return request.post('/api/v1/training/exercises', data)
}

/** PUT /training/exercises/{id} —— 修改动作 */
export function updateExercise(id, data) {
  return request.put(`/api/v1/training/exercises/${id}`, data)
}

/** DELETE /training/exercises/{id} —— 删除动作（系统动作不可删） */
export function deleteExercise(id) {
  return request.delete(`/api/v1/training/exercises/${id}`)
}

/** 辅助：获取全部动作（不分页，用于下拉选择器） */
export function getAllExercises() {
  return request.get('/api/v1/training/exercises', { params: { size: 500 } })
    .then(r => r.list || [])
}

/* ==================== 训练计划 WorkoutPlan（只读） ==================== */

/** GET /training/plans —— 计划列表（分页） */
export function getPlanList(params) {
  return request.get('/api/v1/training/plans', { params })
}

/** GET /training/plans/{id} —— 计划详情（含关联动作 exercises） */
export function getPlanDetail(id) {
  return request.get(`/api/v1/training/plans/${id}`)
}

/** DELETE /training/plans/{id} —— 删除计划 */
export function deletePlan(id) {
  return request.delete(`/api/v1/training/plans/${id}`)
}

/** 辅助：获取全部计划（不分页，用于下拉筛选） */
export function getAllPlans() {
  return request.get('/api/v1/training/plans', { params: { size: 500 } })
    .then(r => r.list || [])
}

/* ==================== 训练记录 WorkoutRecord（只读） ==================== */

/** GET /training/records —— 训练记录列表（分页 + 日期范围筛选） */
export function getRecordList(params) {
  return request.get('/api/v1/training/records', { params })
}

/** GET /training/records/{id} —— 记录详情（含每组明细 sets） */
export function getRecordDetail(id) {
  return request.get(`/api/v1/training/records/${id}`)
}

/* ==================== 枚举常量（UI 层复用，对齐后端 §四） ==================== */

/** 动作分类（1-8 数字枚举，对齐后端 exercise.category） */
export const CATEGORY_OPTIONS = [
  { value: 1, label: '胸',   color: '#ef4444', tag: 'danger'  },
  { value: 2, label: '背',   color: '#3b82f6', tag: 'primary' },
  { value: 3, label: '肩',   color: '#f59e0b', tag: 'warning' },
  { value: 4, label: '手臂', color: '#10b981', tag: 'success' },
  { value: 5, label: '腿',   color: '#06b6d4', tag: 'info'    },
  { value: 6, label: '核心', color: '#8b5cf6', tag: 'purple'  },
  { value: 7, label: '有氧', color: '#06b6d4', tag: 'cyan'    },
  { value: 8, label: '全身', color: '#6b7280', tag: ''        }
]

/** 动作难度（1-3） */
export const DIFFICULTY_OPTIONS = [
  { value: 1, label: '入门', color: '#52c41a', tag: 'success' },
  { value: 2, label: '中级', color: '#faad14', tag: 'warning' },
  { value: 3, label: '高级', color: '#ff4d4f', tag: 'danger'  }
]

/** 器材类型 */
export const EQUIPMENT_OPTIONS = [
  { value: 'barbell',    label: '杠铃' },
  { value: 'dumbbell',   label: '哑铃' },
  { value: 'machine',    label: '固定器械' },
  { value: 'cable',      label: '绳索' },
  { value: 'bodyweight', label: '自重' },
  { value: 'kettlebell', label: '壶铃' },
  { value: 'band',       label: '弹力带' }
]

/** 计划类型（1-3） */
export const PLAN_TYPE_OPTIONS = [
  { value: 1, label: '力量', color: '#3b82f6' },
  { value: 2, label: '有氧', color: '#10b981' },
  { value: 3, label: '混合', color: '#f59e0b' }
]

/** 计划状态枚举 */
export const PLAN_STATUS = {
  DRAFT: 0,       // 草稿
  IN_PROGRESS: 1, // 进行中
  COMPLETED: 2,   // 已完成
  CANCELLED: 3    // 已取消
}

export const PLAN_STATUS_TEXT = {
  0: '草稿',
  1: '进行中',
  2: '已完成',
  3: '已取消'
}
