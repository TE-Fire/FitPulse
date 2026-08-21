/**
 * 训练模块 Mock 数据（动作库 / 训练计划 / 训练记录）
 * 对齐后端接口契约：
 *   - ExerciseVO / ExercisePageVO
 *   - WorkoutPlanListVO / WorkoutPlanDetailVO
 *   - WorkoutRecordListVO / WorkoutRecordDetailVO
 * 注意：后端接口未就绪前，本模块强制使用 mock（忽略 .env 的 USE_MOCK）
 */

function delay(ms = 260) {
  return new Promise(r => setTimeout(r, ms))
}

// ========== 枚举常量 ==========
export const CATEGORY_OPTIONS = [
  { value: 'chest',    label: '胸部', color: '#FF6B6B' },
  { value: 'back',     label: '背部', color: '#4ECDC4' },
  { value: 'leg',      label: '腿部', color: '#45B7D1' },
  { value: 'shoulder', label: '肩部', color: '#96CEB4' },
  { value: 'arm',      label: '手臂', color: '#FFEAA7' },
  { value: 'core',     label: '核心', color: '#DDA0DD' },
  { value: 'cardio',   label: '有氧', color: '#FF8C69' }
]

export const DIFFICULTY_OPTIONS = [
  { value: 1, label: '入门', color: '#52c41a' },
  { value: 2, label: '进阶', color: '#faad14' },
  { value: 3, label: '达人', color: '#ff4d4f' }
]

export const EQUIPMENT_OPTIONS = [
  { value: 'barbell',   label: '杠铃' },
  { value: 'dumbbell',  label: '哑铃' },
  { value: 'machine',   label: '固定器械' },
  { value: 'cable',     label: '绳索' },
  { value: 'bodyweight',label: '自重' },
  { value: 'kettlebell',label: '壶铃' },
  { value: 'band',      label: '弹力带' }
]

// ========== 预置 10 个系统动作（对齐后端 P5 预置） ==========
const MOCK_EXERCISES = [
  {
    id: '1893456789012345001',
    name: '杠铃卧推',
    category: 'chest',
    difficulty: 2,
    muscleGroup: '胸大肌、肱三头肌、三角肌前束',
    equipment: 'barbell',
    description: '平躺在卧推凳上，双脚稳定踩地，双手握距略宽于肩，下放杠铃至胸部中下沿，发力推起至手臂微曲。保持肩胛骨收紧，腰部自然弓起。',
    imageUrl: null,
    isSystem: true
  },
  {
    id: '1893456789012345002',
    name: '哑铃飞鸟',
    category: 'chest',
    difficulty: 1,
    muscleGroup: '胸大肌',
    equipment: 'dumbbell',
    description: '仰卧于平凳，双手持哑铃举于胸部正上方，微曲手肘，向两侧下放哑铃至感受胸部拉伸，发力夹胸回到起始位置。',
    imageUrl: null,
    isSystem: true
  },
  {
    id: '1893456789012345003',
    name: '引体向上',
    category: 'back',
    difficulty: 3,
    muscleGroup: '背阔肌、大圆肌、肱二头肌',
    equipment: 'bodyweight',
    description: '正手握杠，握距略宽于肩，身体自然下垂，发力将下巴拉过单杠，控制下放至手臂完全伸直。',
    imageUrl: null,
    isSystem: true
  },
  {
    id: '1893456789012345004',
    name: '杠铃划船',
    category: 'back',
    difficulty: 2,
    muscleGroup: '背阔肌、斜方肌中下部、菱形肌',
    equipment: 'barbell',
    description: '膝盖微曲，背部挺直前倾约45°，正手握杠铃沿大腿前侧拉起至下腹，挤压肩胛骨后控制下放。',
    imageUrl: null,
    isSystem: true
  },
  {
    id: '1893456789012345005',
    name: '杠铃深蹲',
    category: 'leg',
    difficulty: 3,
    muscleGroup: '股四头肌、臀大肌、腘绳肌',
    equipment: 'barbell',
    description: '杠铃置于斜方肌上部，双脚与肩同宽，脚尖外展30°，髋关节后坐下蹲至大腿平行地面，发力站起。',
    imageUrl: null,
    isSystem: true
  },
  {
    id: '1893456789012345006',
    name: '罗马尼亚硬拉',
    category: 'leg',
    difficulty: 2,
    muscleGroup: '腘绳肌、臀大肌、竖脊肌',
    equipment: 'barbell',
    description: '双脚与髋同宽，微曲膝盖，杠铃沿胫骨前侧下放至感受腿后侧强烈拉伸，臀部发力髋伸展站起。',
    imageUrl: null,
    isSystem: true
  },
  {
    id: '1893456789012345007',
    name: '哑铃推举',
    category: 'shoulder',
    difficulty: 2,
    muscleGroup: '三角肌前中束、肱三头肌',
    equipment: 'dumbbell',
    description: '坐姿或站姿，双手持哑铃举至耳侧，发力推起至手臂微曲，控制下放至大臂平行地面。',
    imageUrl: null,
    isSystem: true
  },
  {
    id: '1893456789012345008',
    name: '杠铃弯举',
    category: 'arm',
    difficulty: 1,
    muscleGroup: '肱二头肌',
    equipment: 'barbell',
    description: '站立，双手反握杠铃，大臂紧贴身体固定，弯举杠铃至收缩感最强，控制下放至完全伸直。',
    imageUrl: null,
    isSystem: true
  },
  {
    id: '1893456789012345009',
    name: '平板支撑',
    category: 'core',
    difficulty: 1,
    muscleGroup: '腹横肌、多裂肌、盆底肌',
    equipment: 'bodyweight',
    description: '双肘撑地与肩同宽，身体呈一条直线，核心收紧不塌腰不撅臀，保持均匀呼吸。',
    imageUrl: null,
    isSystem: true
  },
  {
    id: '1893456789012345010',
    name: '跑步机慢跑',
    category: 'cardio',
    difficulty: 1,
    muscleGroup: '全身有氧',
    equipment: 'machine',
    description: '调节坡度与速度至舒适心率区间（最大心率60-75%），保持均匀步伐与呼吸节奏。',
    imageUrl: null,
    isSystem: true
  },
  // 2 个用户自建示例
  {
    id: '1893456789012345011',
    name: '绳索夹胸',
    category: 'chest',
    difficulty: 2,
    muscleGroup: '胸大肌（胸中缝）',
    equipment: 'cable',
    description: '高位绳索，双手握D型把手，身体微前倾，双臂微曲从两侧向中间夹胸，顶峰收缩停顿。',
    imageUrl: null,
    isSystem: false
  },
  {
    id: '1893456789012345012',
    name: '保加利亚分腿蹲',
    category: 'leg',
    difficulty: 3,
    muscleGroup: '股四头肌、臀大肌、稳定肌群',
    equipment: 'dumbbell',
    description: '后脚置于长凳，前脚距凳约一大步，下蹲至前腿大腿平行地面，发力站起。',
    imageUrl: null,
    isSystem: false
  }
]

// ========== 训练计划示例 ==========
const MOCK_PLANS = [
  {
    id: '1893456789012345101',
    name: '推日A',
    description: '胸 + 三头 + 前束三角肌（4 动作，推类为主）',
    status: 1,
    itemCount: 4,
    createdAt: '2026-08-10T10:30:00',
    items: [
      { id: '1', exerciseId: '1893456789012345001', exerciseName: '杠铃卧推', sortOrder: 1, targetSets: 4, targetReps: 8,  restSeconds: 120 },
      { id: '2', exerciseId: '1893456789012345007', exerciseName: '哑铃推举', sortOrder: 2, targetSets: 4, targetReps: 10, restSeconds: 90  },
      { id: '3', exerciseId: '1893456789012345002', exerciseName: '哑铃飞鸟', sortOrder: 3, targetSets: 3, targetReps: 12, restSeconds: 60  },
      { id: '4', exerciseId: '1893456789012345011', exerciseName: '绳索夹胸', sortOrder: 4, targetSets: 3, targetReps: 15, restSeconds: 60  }
    ]
  },
  {
    id: '1893456789012345102',
    name: '拉日A',
    description: '背 + 二头（垂直拉 + 水平拉组合）',
    status: 1,
    itemCount: 3,
    createdAt: '2026-08-12T14:20:00',
    items: [
      { id: '1', exerciseId: '1893456789012345003', exerciseName: '引体向上', sortOrder: 1, targetSets: 5, targetReps: 6,  restSeconds: 120 },
      { id: '2', exerciseId: '1893456789012345004', exerciseName: '杠铃划船', sortOrder: 2, targetSets: 4, targetReps: 8,  restSeconds: 90  },
      { id: '3', exerciseId: '1893456789012345008', exerciseName: '杠铃弯举', sortOrder: 3, targetSets: 3, targetReps: 12, restSeconds: 60  }
    ]
  },
  {
    id: '1893456789012345103',
    name: '腿日A',
    description: '下肢爆发训练（深蹲 + 硬拉双主项）',
    status: 1,
    itemCount: 4,
    createdAt: '2026-08-15T09:00:00',
    items: [
      { id: '1', exerciseId: '1893456789012345005', exerciseName: '杠铃深蹲',     sortOrder: 1, targetSets: 5, targetReps: 5,  restSeconds: 180 },
      { id: '2', exerciseId: '1893456789012345006', exerciseName: '罗马尼亚硬拉', sortOrder: 2, targetSets: 4, targetReps: 8,  restSeconds: 120 },
      { id: '3', exerciseId: '1893456789012345012', exerciseName: '保加利亚分腿蹲', sortOrder: 3, targetSets: 3, targetReps: 10, restSeconds: 90  },
      { id: '4', exerciseId: '1893456789012345009', exerciseName: '平板支撑',     sortOrder: 4, targetSets: 3, targetReps: 1,  restSeconds: 60  }
    ]
  }
]

// ========== 训练记录示例 ==========
const MOCK_RECORDS = [
  {
    id: '1893456789012345201',
    planId: '1893456789012345101',
    planName: '推日A',
    recordDate: '2026-08-19',
    durationSec: 3900,
    totalVolume: 5400,
    totalSets: 14,
    totalReps: 104,
    note: '今天状态不错，卧推加到了80kg做组，飞鸟找到顶峰收缩的感觉了。',
    sets: [
      { id: 's1', exerciseId: '1893456789012345001', exerciseName: '杠铃卧推', setNo: 1, weightKg: 60, reps: 10, rpe: 6, restSeconds: 120 },
      { id: 's2', exerciseId: '1893456789012345001', exerciseName: '杠铃卧推', setNo: 2, weightKg: 70, reps: 8,  rpe: 7, restSeconds: 120 },
      { id: 's3', exerciseId: '1893456789012345001', exerciseName: '杠铃卧推', setNo: 3, weightKg: 80, reps: 6,  rpe: 8, restSeconds: 150 },
      { id: 's4', exerciseId: '1893456789012345001', exerciseName: '杠铃卧推', setNo: 4, weightKg: 80, reps: 5,  rpe: 9, restSeconds: 150 },
      { id: 's5', exerciseId: '1893456789012345007', exerciseName: '哑铃推举', setNo: 1, weightKg: 22, reps: 10, rpe: 6, restSeconds: 90  },
      { id: 's6', exerciseId: '1893456789012345007', exerciseName: '哑铃推举', setNo: 2, weightKg: 24, reps: 10, rpe: 7, restSeconds: 90  },
      { id: 's7', exerciseId: '1893456789012345007', exerciseName: '哑铃推举', setNo: 3, weightKg: 24, reps: 9,  rpe: 8, restSeconds: 90  },
      { id: 's8', exerciseId: '1893456789012345007', exerciseName: '哑铃推举', setNo: 4, weightKg: 24, reps: 8,  rpe: 8, restSeconds: 90  },
      { id: 's9', exerciseId: '1893456789012345002', exerciseName: '哑铃飞鸟', setNo: 1, weightKg: 14, reps: 12, rpe: 7, restSeconds: 60  },
      { id: 's10', exerciseId: '1893456789012345002', exerciseName: '哑铃飞鸟', setNo: 2, weightKg: 14, reps: 12, rpe: 7, restSeconds: 60  },
      { id: 's11', exerciseId: '1893456789012345002', exerciseName: '哑铃飞鸟', setNo: 3, weightKg: 14, reps: 11, rpe: 8, restSeconds: 60  },
      { id: 's12', exerciseId: '1893456789012345011', exerciseName: '绳索夹胸', setNo: 1, weightKg: 30, reps: 15, rpe: 7, restSeconds: 60  },
      { id: 's13', exerciseId: '1893456789012345011', exerciseName: '绳索夹胸', setNo: 2, weightKg: 30, reps: 15, rpe: 8, restSeconds: 60  },
      { id: 's14', exerciseId: '1893456789012345011', exerciseName: '绳索夹胸', setNo: 3, weightKg: 30, reps: 14, rpe: 8, restSeconds: 60  }
    ]
  },
  {
    id: '1893456789012345202',
    planId: '1893456789012345102',
    planName: '拉日A',
    recordDate: '2026-08-17',
    durationSec: 3300,
    totalVolume: 4280,
    totalSets: 12,
    totalReps: 80,
    note: '引体向上第一次能做 6x5，背部进步明显！',
    sets: [
      { id: 'r1', exerciseId: '1893456789012345003', exerciseName: '引体向上', setNo: 1, weightKg: 0, reps: 6, rpe: 8, restSeconds: 120 },
      { id: 'r2', exerciseId: '1893456789012345003', exerciseName: '引体向上', setNo: 2, weightKg: 0, reps: 6, rpe: 8, restSeconds: 120 },
      { id: 'r3', exerciseId: '1893456789012345003', exerciseName: '引体向上', setNo: 3, weightKg: 0, reps: 5, rpe: 9, restSeconds: 150 },
      { id: 'r4', exerciseId: '1893456789012345003', exerciseName: '引体向上', setNo: 4, weightKg: 0, reps: 5, rpe: 9, restSeconds: 150 },
      { id: 'r5', exerciseId: '1893456789012345003', exerciseName: '引体向上', setNo: 5, weightKg: 0, reps: 4, rpe: 9, restSeconds: 150 },
      { id: 'r6', exerciseId: '1893456789012345004', exerciseName: '杠铃划船', setNo: 1, weightKg: 50, reps: 10, rpe: 6, restSeconds: 90  },
      { id: 'r7', exerciseId: '1893456789012345004', exerciseName: '杠铃划船', setNo: 2, weightKg: 60, reps: 8,  rpe: 7, restSeconds: 90  },
      { id: 'r8', exerciseId: '1893456789012345004', exerciseName: '杠铃划船', setNo: 3, weightKg: 60, reps: 8,  rpe: 8, restSeconds: 90  },
      { id: 'r9', exerciseId: '1893456789012345004', exerciseName: '杠铃划船', setNo: 4, weightKg: 65, reps: 7,  rpe: 8, restSeconds: 90  },
      { id: 'r10', exerciseId: '1893456789012345008', exerciseName: '杠铃弯举', setNo: 1, weightKg: 20, reps: 12, rpe: 7, restSeconds: 60 },
      { id: 'r11', exerciseId: '1893456789012345008', exerciseName: '杠铃弯举', setNo: 2, weightKg: 20, reps: 12, rpe: 7, restSeconds: 60 },
      { id: 'r12', exerciseId: '1893456789012345008', exerciseName: '杠铃弯举', setNo: 3, weightKg: 22, reps: 10, rpe: 8, restSeconds: 60 }
    ]
  },
  {
    id: '1893456789012345203',
    planId: '1893456789012345103',
    planName: '腿日A',
    recordDate: '2026-08-15',
    durationSec: 4200,
    totalVolume: 7950,
    totalSets: 15,
    totalReps: 70,
    note: '深蹲100kg x 5 完成，PR！',
    sets: [
      { id: 'l1', exerciseId: '1893456789012345005', exerciseName: '杠铃深蹲', setNo: 1, weightKg: 60,  reps: 8, rpe: 5, restSeconds: 180 },
      { id: 'l2', exerciseId: '1893456789012345005', exerciseName: '杠铃深蹲', setNo: 2, weightKg: 80,  reps: 5, rpe: 6, restSeconds: 180 },
      { id: 'l3', exerciseId: '1893456789012345005', exerciseName: '杠铃深蹲', setNo: 3, weightKg: 90,  reps: 5, rpe: 7, restSeconds: 210 },
      { id: 'l4', exerciseId: '1893456789012345005', exerciseName: '杠铃深蹲', setNo: 4, weightKg: 100, reps: 5, rpe: 9, restSeconds: 240 },
      { id: 'l5', exerciseId: '1893456789012345005', exerciseName: '杠铃深蹲', setNo: 5, weightKg: 100, reps: 5, rpe: 9, restSeconds: 240 },
      { id: 'l6', exerciseId: '1893456789012345006', exerciseName: '罗马尼亚硬拉', setNo: 1, weightKg: 60, reps: 10, rpe: 5, restSeconds: 120 },
      { id: 'l7', exerciseId: '1893456789012345006', exerciseName: '罗马尼亚硬拉', setNo: 2, weightKg: 80, reps: 8,  rpe: 7, restSeconds: 120 },
      { id: 'l8', exerciseId: '1893456789012345006', exerciseName: '罗马尼亚硬拉', setNo: 3, weightKg: 90, reps: 8,  rpe: 8, restSeconds: 120 },
      { id: 'l9', exerciseId: '1893456789012345006', exerciseName: '罗马尼亚硬拉', setNo: 4, weightKg: 90, reps: 7,  rpe: 8, restSeconds: 120 },
      { id: 'l10', exerciseId: '1893456789012345012', exerciseName: '保加利亚分腿蹲', setNo: 1, weightKg: 16, reps: 10, rpe: 6, restSeconds: 90 },
      { id: 'l11', exerciseId: '1893456789012345012', exerciseName: '保加利亚分腿蹲', setNo: 2, weightKg: 16, reps: 10, rpe: 7, restSeconds: 90 },
      { id: 'l12', exerciseId: '1893456789012345012', exerciseName: '保加利亚分腿蹲', setNo: 3, weightKg: 18, reps: 9,  rpe: 8, restSeconds: 90 },
      { id: 'l13', exerciseId: '1893456789012345009', exerciseName: '平板支撑', setNo: 1, weightKg: 0, reps: 1, rpe: 7, restSeconds: 60 },
      { id: 'l14', exerciseId: '1893456789012345009', exerciseName: '平板支撑', setNo: 2, weightKg: 0, reps: 1, rpe: 7, restSeconds: 60 },
      { id: 'l15', exerciseId: '1893456789012345009', exerciseName: '平板支撑', setNo: 3, weightKg: 0, reps: 1, rpe: 8, restSeconds: 60 }
    ]
  }
]

// ========== Mock 状态（模拟内存数据库，支持 CRUD） ==========
let exercisesSeq = 200
let plansSeq     = 200
let recordsSeq   = 300

const exercisesStore = [...MOCK_EXERCISES]
const plansStore     = [...MOCK_PLANS]
const recordsStore   = [...MOCK_RECORDS]

function nextId(prefix) {
  if (prefix === 'e') return String(1893456789012345000 + (++exercisesSeq))
  if (prefix === 'p') return String(1893456789012345200 + (++plansSeq))
  return String(1893456789012345400 + (++recordsSeq))
}

// ========== 分页工具 ==========
function paginate(list, page, size) {
  const p = Math.max(1, page || 1)
  const s = Math.max(1, Math.min(100, size || 10))
  const total = list.length
  const start = (p - 1) * s
  return {
    records: list.slice(start, start + s),
    total,
    page: p,
    size: s
  }
}

// ========== 动作库接口 ==========

export async function getExerciseList(params = {}) {
  await delay()
  let list = [...exercisesStore]
  if (params.name) {
    const kw = String(params.name).toLowerCase()
    list = list.filter(x => x.name.toLowerCase().includes(kw))
  }
  if (params.category) {
    list = list.filter(x => x.category === params.category)
  }
  if (params.difficulty) {
    list = list.filter(x => x.difficulty === Number(params.difficulty))
  }
  // 默认系统动作排前面，再按名称
  list.sort((a, b) => (b.isSystem - a.isSystem) || a.name.localeCompare(b.name, 'zh-Hans'))
  return paginate(list, params.page, params.size)
}

export async function getExerciseDetail(id) {
  await delay()
  const found = exercisesStore.find(x => x.id === id)
  if (!found) throw new Error('EXERCISE_NOT_FOUND')
  return { ...found }
}

export async function createExercise(payload) {
  await delay()
  const dup = exercisesStore.find(x => x.name === payload.name)
  if (dup) throw new Error('EXERCISE_NAME_DUPLICATED')
  const record = {
    id: nextId('e'),
    name: payload.name,
    category: payload.category,
    difficulty: Number(payload.difficulty),
    muscleGroup: payload.muscleGroup || '',
    equipment: payload.equipment || '',
    description: payload.description || '',
    imageUrl: payload.imageUrl || null,
    isSystem: false
  }
  exercisesStore.unshift(record)
  return { id: record.id }
}

export async function updateExercise(id, payload) {
  await delay()
  const idx = exercisesStore.findIndex(x => x.id === id)
  if (idx < 0) throw new Error('EXERCISE_NOT_FOUND')
  const dup = exercisesStore.find(x => x.name === payload.name && x.id !== id)
  if (dup) throw new Error('EXERCISE_NAME_DUPLICATED')
  exercisesStore[idx] = {
    ...exercisesStore[idx],
    name: payload.name,
    category: payload.category,
    difficulty: Number(payload.difficulty),
    muscleGroup: payload.muscleGroup || '',
    equipment: payload.equipment || '',
    description: payload.description || '',
    imageUrl: payload.imageUrl || null
  }
  return { id }
}

export async function deleteExercise(id) {
  await delay()
  const idx = exercisesStore.findIndex(x => x.id === id)
  if (idx < 0) throw new Error('EXERCISE_NOT_FOUND')
  if (exercisesStore[idx].isSystem) throw new Error('EXERCISE_SYSTEM_CANNOT_DELETE')
  const usedInPlan = plansStore.some(p => p.items && p.items.some(i => i.exerciseId === id))
  const usedInRecord = recordsStore.some(r => r.sets && r.sets.some(s => s.exerciseId === id))
  if (usedInPlan || usedInRecord) throw new Error('EXERCISE_IN_USE')
  exercisesStore.splice(idx, 1)
  return null
}

// ========== 训练计划接口 ==========

export async function getPlanList(params = {}) {
  await delay()
  let list = plansStore.map(p => ({
    id: p.id,
    name: p.name,
    description: p.description,
    status: p.status,
    itemCount: p.items ? p.items.length : 0,
    createdAt: p.createdAt
  }))
  list.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
  return paginate(list, params.page, params.size)
}

export async function getPlanDetail(id) {
  await delay()
  const p = plansStore.find(x => x.id === id)
  if (!p) throw new Error('PLAN_NOT_FOUND')
  return {
    id: p.id,
    name: p.name,
    description: p.description,
    status: p.status,
    items: (p.items || []).map(i => ({ ...i }))
  }
}

export async function createPlan(payload) {
  await delay()
  const dup = plansStore.find(x => x.name === payload.name)
  if (dup) throw new Error('PLAN_NAME_DUPLICATED')
  if (!payload.items || payload.items.length === 0) throw new Error('PLAN_ITEM_EMPTY')
  const id = nextId('p')
  const record = {
    id,
    name: payload.name,
    description: payload.description || '',
    status: 1,
    createdAt: new Date().toISOString().slice(0, 19).replace('T', 'T'),
    items: payload.items.map((it, idx) => ({
      id: String(idx + 1),
      exerciseId: it.exerciseId,
      exerciseName: exercisesStore.find(e => e.id === it.exerciseId)?.name || '',
      sortOrder: it.sortOrder || (idx + 1),
      targetSets: it.targetSets,
      targetReps: it.targetReps,
      restSeconds: it.restSeconds ?? 60
    }))
  }
  plansStore.unshift(record)
  return { id }
}

export async function updatePlan(id, payload) {
  await delay()
  const idx = plansStore.findIndex(x => x.id === id)
  if (idx < 0) throw new Error('PLAN_NOT_FOUND')
  const dup = plansStore.find(x => x.name === payload.name && x.id !== id)
  if (dup) throw new Error('PLAN_NAME_DUPLICATED')
  if (!payload.items || payload.items.length === 0) throw new Error('PLAN_ITEM_EMPTY')
  // 全量替换 items
  plansStore[idx] = {
    ...plansStore[idx],
    name: payload.name,
    description: payload.description || '',
    items: payload.items.map((it, i) => ({
      id: it.id || String(i + 1),
      exerciseId: it.exerciseId,
      exerciseName: exercisesStore.find(e => e.id === it.exerciseId)?.name || '',
      sortOrder: it.sortOrder || (i + 1),
      targetSets: it.targetSets,
      targetReps: it.targetReps,
      restSeconds: it.restSeconds ?? 60
    }))
  }
  return { id }
}

export async function deletePlan(id) {
  await delay()
  const idx = plansStore.findIndex(x => x.id === id)
  if (idx < 0) throw new Error('PLAN_NOT_FOUND')
  plansStore.splice(idx, 1)
  return null
}

// ========== 训练记录接口 ==========

export async function getRecordList(params = {}) {
  await delay()
  let list = recordsStore.map(r => ({
    id: r.id,
    planId: r.planId,
    planName: r.planName,
    recordDate: r.recordDate,
    durationSec: r.durationSec,
    totalVolume: r.totalVolume,
    totalSets: r.totalSets,
    totalReps: r.totalReps,
    note: r.note
  }))
  if (params.startDate) {
    list = list.filter(r => r.recordDate >= params.startDate)
  }
  if (params.endDate) {
    list = list.filter(r => r.recordDate <= params.endDate)
  }
  list.sort((a, b) => b.recordDate.localeCompare(a.recordDate))
  return paginate(list, params.page, params.size)
}

export async function getRecordDetail(id) {
  await delay()
  const r = recordsStore.find(x => x.id === id)
  if (!r) throw new Error('RECORD_NOT_FOUND')
  return {
    id: r.id,
    planId: r.planId,
    planName: r.planName,
    recordDate: r.recordDate,
    durationSec: r.durationSec,
    totalVolume: r.totalVolume,
    totalSets: r.totalSets,
    totalReps: r.totalReps,
    note: r.note,
    sets: (r.sets || []).map(s => ({ ...s }))
  }
}

export async function createRecord(payload) {
  await delay()
  if (!payload.sets || payload.sets.length === 0) throw new Error('RECORD_SET_EMPTY')
  // 容量自动计算
  const totalVolume = payload.sets.reduce((s, x) => s + (Number(x.weightKg) || 0) * (Number(x.reps) || 0), 0)
  const totalSets   = payload.sets.length
  const totalReps   = payload.sets.reduce((s, x) => s + (Number(x.reps) || 0), 0)
  const id = nextId('r')
  const planName = payload.planId ? (plansStore.find(p => p.id === payload.planId)?.name || '') : ''
  const record = {
    id,
    planId: payload.planId || null,
    planName,
    recordDate: payload.recordDate,
    durationSec: payload.durationSec || 0,
    totalVolume,
    totalSets,
    totalReps,
    note: payload.note || '',
    sets: payload.sets.map((s, i) => ({
      id: 'rs' + i,
      exerciseId: s.exerciseId,
      exerciseName: exercisesStore.find(e => e.id === s.exerciseId)?.name || '',
      setNo: s.setNo || (i + 1),
      weightKg: Number(s.weightKg) || 0,
      reps: Number(s.reps) || 0,
      rpe: s.rpe || null,
      restSeconds: s.restSeconds ?? 0
    }))
  }
  recordsStore.unshift(record)
  return { id }
}

// 辅助：获取所有动作（用于计划编辑页的选择器，不分页）
export async function getAllExercises() {
  await delay(120)
  return [...exercisesStore].sort((a, b) => (b.isSystem - a.isSystem) || a.name.localeCompare(b.name, 'zh-Hans'))
}

// 辅助：获取所有计划（用于记录录入页的下拉选择，不分页）
export async function getAllPlans() {
  await delay(120)
  return plansStore
    .filter(p => p.status === 1)
    .map(p => ({ id: p.id, name: p.name, description: p.description, items: p.items || [] }))
    .sort((a, b) => a.name.localeCompare(b.name, 'zh-Hans'))
}
