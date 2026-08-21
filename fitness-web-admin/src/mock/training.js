/**
 * 训练模块 Mock 数据（动作库 / 训练计划 / 训练记录）
 * 严格对齐后端接口契约（schema.sql + V3 迁移）：
 *   - exercise.category: TINYINT 1-8（胸/背/肩/手臂/腿/核心/有氧/全身）
 *   - exercise.difficulty: TINYINT 1-3
 *   - workout_plan.plan_type: TINYINT 1-3（力量/有氧/混合）
 *   - workout_plan.status: TINYINT 0-3（DRAFT/IN_PROGRESS/COMPLETED/CANCELLED）
 *   - workout_plan_exercise.target_reps: VARCHAR（字符串，如 "8-12"、"12,10,8"、"力竭"）
 *   - workout_plan_exercise.rest_sec: INT（字段名单数）
 *   - workout_plan_exercise.target_weight_kg: DECIMAL(8,2)
 *   - workout_set: 含 is_completed / is_warmup / rpe，无 rest_sec
 * 注意：后端接口未就绪前，本模块强制使用 mock（忽略 .env 的 USE_MOCK）
 */

function delay(ms = 260) {
  return new Promise(r => setTimeout(r, ms))
}

// ========== 枚举常量（对齐后端 §四.1 CATEGORY_MAP）==========
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

export const DIFFICULTY_OPTIONS = [
  { value: 1, label: '入门', color: '#52c41a', tag: 'success' },
  { value: 2, label: '中级', color: '#faad14', tag: 'warning' },
  { value: 3, label: '高级', color: '#ff4d4f', tag: 'danger'  }
]

export const EQUIPMENT_OPTIONS = [
  { value: 'barbell',    label: '杠铃' },
  { value: 'dumbbell',   label: '哑铃' },
  { value: 'machine',    label: '固定器械' },
  { value: 'cable',      label: '绳索' },
  { value: 'bodyweight', label: '自重' },
  { value: 'kettlebell', label: '壶铃' },
  { value: 'band',       label: '弹力带' }
]

// 计划类型（对齐后端 §四.2 PLAN_TYPE_MAP）
export const PLAN_TYPE_OPTIONS = [
  { value: 1, label: '力量', color: '#3b82f6' },
  { value: 2, label: '有氧', color: '#10b981' },
  { value: 3, label: '混合', color: '#f59e0b' }
]

// ========== 预置 10 个系统动作（对齐后端 §二.4）==========
const MOCK_EXERCISES = [
  {
    id: '2089345678901234001',
    name: '杠铃卧推',
    category: 1,
    difficulty: 2,
    muscleGroup: '胸大肌、肱三头肌、三角肌前束',
    equipment: 'barbell',
    description: '平躺在卧推凳上，双脚稳定踩地，双手握距略宽于肩，下放杠铃至胸部中下沿，发力推起至手臂微曲。保持肩胛骨收紧，腰部自然弓起。',
    imageUrl: null,
    isSystem: true,
    categoryLabel: '胸',
    difficultyLabel: '中级'
  },
  {
    id: '2089345678901234002',
    name: '哑铃飞鸟',
    category: 1,
    difficulty: 1,
    muscleGroup: '胸大肌',
    equipment: 'dumbbell',
    description: '仰卧于平凳，双手持哑铃举于胸部正上方，微曲手肘，向两侧下放哑铃至感受胸部拉伸，发力夹胸回到起始位置。',
    imageUrl: null,
    isSystem: true,
    categoryLabel: '胸',
    difficultyLabel: '入门'
  },
  {
    id: '2089345678901234003',
    name: '引体向上',
    category: 2,
    difficulty: 3,
    muscleGroup: '背阔肌、大圆肌、肱二头肌',
    equipment: 'bodyweight',
    description: '正手握杠，握距略宽于肩，身体自然下垂，发力将下巴拉过单杠，控制下放至手臂完全伸直。',
    imageUrl: null,
    isSystem: true,
    categoryLabel: '背',
    difficultyLabel: '高级'
  },
  {
    id: '2089345678901234004',
    name: '坐姿划船',
    category: 2,
    difficulty: 1,
    muscleGroup: '背阔肌、斜方肌中下部、菱形肌',
    equipment: 'machine',
    description: '坐姿，双脚稳定踏地，挺胸收腹，双手握把手沿身体两侧拉至下腹，挤压肩胛骨后控制下放。',
    imageUrl: null,
    isSystem: true,
    categoryLabel: '背',
    difficultyLabel: '入门'
  },
  {
    id: '2089345678901234005',
    name: '杠铃深蹲',
    category: 5,
    difficulty: 3,
    muscleGroup: '股四头肌、臀大肌、腘绳肌',
    equipment: 'barbell',
    description: '杠铃置于斜方肌上部，双脚与肩同宽，脚尖外展30°，髋关节后坐下蹲至大腿平行地面，发力站起。',
    imageUrl: null,
    isSystem: true,
    categoryLabel: '腿',
    difficultyLabel: '高级'
  },
  {
    id: '2089345678901234006',
    name: '腿举',
    category: 5,
    difficulty: 1,
    muscleGroup: '股四头肌、臀大肌',
    equipment: 'machine',
    description: '坐姿背靠靠背，双脚与肩同宽踩踏板，脚尖微外展，下放踏板至膝盖角度约90°，发力蹬起至腿微曲。',
    imageUrl: null,
    isSystem: true,
    categoryLabel: '腿',
    difficultyLabel: '入门'
  },
  {
    id: '2089345678901234007',
    name: '杠铃推举',
    category: 3,
    difficulty: 2,
    muscleGroup: '三角肌前中束、肱三头肌',
    equipment: 'barbell',
    description: '站姿或坐姿，双手持杠铃举至锁骨高度，发力推起至手臂微曲，控制下放。',
    imageUrl: null,
    isSystem: true,
    categoryLabel: '肩',
    difficultyLabel: '中级'
  },
  {
    id: '2089345678901234008',
    name: '哑铃侧平举',
    category: 3,
    difficulty: 1,
    muscleGroup: '三角肌中束',
    equipment: 'dumbbell',
    description: '站姿，双手持哑铃于身体两侧，微曲手肘，向两侧抬举哑铃至与肩平行，控制下放。',
    imageUrl: null,
    isSystem: true,
    categoryLabel: '肩',
    difficultyLabel: '入门'
  },
  {
    id: '2089345678901234009',
    name: '杠铃弯举',
    category: 4,
    difficulty: 1,
    muscleGroup: '肱二头肌',
    equipment: 'barbell',
    description: '站立，双手反握杠铃，大臂紧贴身体固定，弯举杠铃至收缩感最强，控制下放至完全伸直。',
    imageUrl: null,
    isSystem: true,
    categoryLabel: '手臂',
    difficultyLabel: '入门'
  },
  {
    id: '2089345678901234010',
    name: '平板支撑',
    category: 6,
    difficulty: 1,
    muscleGroup: '腹横肌、多裂肌、盆底肌',
    equipment: 'bodyweight',
    description: '双肘撑地与肩同宽，身体呈一条直线，核心收紧不塌腰不撅臀，保持均匀呼吸。',
    imageUrl: null,
    isSystem: true,
    categoryLabel: '核心',
    difficultyLabel: '入门'
  },
  // 用户自建示例（来自移动端同步）
  {
    id: '2089345678901234011',
    name: '绳索夹胸',
    category: 1,
    difficulty: 2,
    muscleGroup: '胸大肌（胸中缝）',
    equipment: 'cable',
    description: '高位绳索，双手握D型把手，身体微前倾，双臂微曲从两侧向中间夹胸，顶峰收缩停顿。',
    imageUrl: null,
    isSystem: false,
    categoryLabel: '胸',
    difficultyLabel: '中级'
  },
  {
    id: '2089345678901234012',
    name: '保加利亚分腿蹲',
    category: 5,
    difficulty: 3,
    muscleGroup: '股四头肌、臀大肌、稳定肌群',
    equipment: 'dumbbell',
    description: '后脚置于长凳，前脚距凳约一大步，下蹲至前腿大腿平行地面，发力站起。',
    imageUrl: null,
    isSystem: false,
    categoryLabel: '腿',
    difficultyLabel: '高级'
  }
]

// ========== 训练计划示例（对齐 WorkoutPlan + WorkoutPlanExercise schema）==========
const MOCK_PLANS = [
  {
    id: '2089345678901234101',
    name: '推日A',
    planType: 1,
    planTypeLabel: '力量',
    description: '胸 + 三头 + 前束三角肌（4 动作，推类为主）',
    estimatedMin: 60,
    status: 0,
    statusText: '草稿',
    exerciseCount: 4,
    userId: 'user1',
    userName: '张三',
    createdAt: '2026-08-10T10:30:00',
    startedAt: null,
    completedAt: null,
    actualDurationSec: null,
    exercises: [
      {
        id: '2089345678901234201',
        planId: '2089345678901234101',
        exerciseId: '2089345678901234001',
        exerciseName: '杠铃卧推',
        sortOrder: 1,
        targetSets: 4,
        targetReps: '8-12',
        targetWeightKg: 60.00,
        restSec: 120
      },
      {
        id: '2089345678901234202',
        planId: '2089345678901234101',
        exerciseId: '2089345678901234007',
        exerciseName: '杠铃推举',
        sortOrder: 2,
        targetSets: 4,
        targetReps: '10',
        targetWeightKg: 40.00,
        restSec: 90
      },
      {
        id: '2089345678901234203',
        planId: '2089345678901234101',
        exerciseId: '2089345678901234002',
        exerciseName: '哑铃飞鸟',
        sortOrder: 3,
        targetSets: 3,
        targetReps: '12',
        targetWeightKg: 20.00,
        restSec: 60
      },
      {
        id: '2089345678901234204',
        planId: '2089345678901234101',
        exerciseId: '2089345678901234011',
        exerciseName: '绳索夹胸',
        sortOrder: 4,
        targetSets: 3,
        targetReps: '15',
        targetWeightKg: 30.00,
        restSec: 60
      }
    ]
  },
  {
    id: '2089345678901234102',
    name: '拉日A',
    planType: 1,
    planTypeLabel: '力量',
    description: '背 + 二头（垂直拉 + 水平拉组合）',
    estimatedMin: 55,
    status: 2,
    statusText: '已完成',
    exerciseCount: 3,
    userId: 'user2',
    userName: '李四',
    createdAt: '2026-08-12T14:20:00',
    startedAt: '2026-08-17T19:00:00',
    completedAt: '2026-08-17T19:55:00',
    actualDurationSec: 3300,
    exercises: [
      {
        id: '2089345678901234210',
        planId: '2089345678901234102',
        exerciseId: '2089345678901234003',
        exerciseName: '引体向上',
        sortOrder: 1,
        targetSets: 5,
        targetReps: '5-6',
        targetWeightKg: 0,
        restSec: 120
      },
      {
        id: '2089345678901234211',
        planId: '2089345678901234102',
        exerciseId: '2089345678901234004',
        exerciseName: '坐姿划船',
        sortOrder: 2,
        targetSets: 4,
        targetReps: '8',
        targetWeightKg: 50.00,
        restSec: 90
      },
      {
        id: '2089345678901234212',
        planId: '2089345678901234102',
        exerciseId: '2089345678901234009',
        exerciseName: '杠铃弯举',
        sortOrder: 3,
        targetSets: 3,
        targetReps: '12',
        targetWeightKg: 20.00,
        restSec: 60
      }
    ]
  },
  {
    id: '2089345678901234103',
    name: '腿日A',
    planType: 1,
    planTypeLabel: '力量',
    description: '下肢爆发训练（深蹲 + 硬拉双主项）',
    estimatedMin: 70,
    status: 0,
    statusText: '草稿',
    exerciseCount: 4,
    userId: 'user3',
    userName: '王五',
    createdAt: '2026-08-15T09:00:00',
    startedAt: null,
    completedAt: null,
    actualDurationSec: null,
    exercises: [
      {
        id: '2089345678901234220',
        planId: '2089345678901234103',
        exerciseId: '2089345678901234005',
        exerciseName: '杠铃深蹲',
        sortOrder: 1,
        targetSets: 5,
        targetReps: '5',
        targetWeightKg: 100.00,
        restSec: 180
      },
      {
        id: '2089345678901234221',
        planId: '2089345678901234103',
        exerciseId: '2089345678901234006',
        exerciseName: '腿举',
        sortOrder: 2,
        targetSets: 4,
        targetReps: '8',
        targetWeightKg: 180.00,
        restSec: 120
      },
      {
        id: '2089345678901234222',
        planId: '2089345678901234103',
        exerciseId: '2089345678901234012',
        exerciseName: '保加利亚分腿蹲',
        sortOrder: 3,
        targetSets: 3,
        targetReps: '10',
        targetWeightKg: 16.00,
        restSec: 90
      },
      {
        id: '2089345678901234223',
        planId: '2089345678901234103',
        exerciseId: '2089345678901234010',
        exerciseName: '平板支撑',
        sortOrder: 4,
        targetSets: 3,
        targetReps: '1',
        targetWeightKg: 0,
        restSec: 60
      }
    ]
  }
]

// ========== 训练记录示例（对齐 WorkoutRecord + WorkoutSet schema）==========
// 注意：workout_set 表中没有 rest_sec（仅 plan_exercise 有）
const MOCK_RECORDS = [
  {
    id: '2089345678901234501',
    userId: 'user1',
    userName: '张三',
    planId: '2089345678901234101',
    planName: '推日A',
    recordDate: '2026-08-19',
    durationSec: 3900,
    totalVolume: 5400.00,
    totalSets: 14,
    totalReps: 104,
    note: '今天状态不错，卧推加到了80kg做组，飞鸟找到顶峰收缩的感觉了。',
    createdAt: '2026-08-19T20:05:00',
    sets: [
      { id: '2089345678901234601', exerciseId: '2089345678901234001', exerciseName: '杠铃卧推', setNo: 1, weightKg: 60.00, reps: 10, isCompleted: 1, isWarmup: 1, rpe: 6 },
      { id: '2089345678901234602', exerciseId: '2089345678901234001', exerciseName: '杠铃卧推', setNo: 2, weightKg: 70.00, reps: 8,  isCompleted: 1, isWarmup: 0, rpe: 7 },
      { id: '2089345678901234603', exerciseId: '2089345678901234001', exerciseName: '杠铃卧推', setNo: 3, weightKg: 80.00, reps: 6,  isCompleted: 1, isWarmup: 0, rpe: 8 },
      { id: '2089345678901234604', exerciseId: '2089345678901234001', exerciseName: '杠铃卧推', setNo: 4, weightKg: 80.00, reps: 5,  isCompleted: 1, isWarmup: 0, rpe: 9 },
      { id: '2089345678901234605', exerciseId: '2089345678901234007', exerciseName: '杠铃推举',   setNo: 1, weightKg: 22.00, reps: 10, isCompleted: 1, isWarmup: 0, rpe: 6 },
      { id: '2089345678901234606', exerciseId: '2089345678901234007', exerciseName: '杠铃推举',   setNo: 2, weightKg: 24.00, reps: 10, isCompleted: 1, isWarmup: 0, rpe: 7 },
      { id: '2089345678901234607', exerciseId: '2089345678901234007', exerciseName: '杠铃推举',   setNo: 3, weightKg: 24.00, reps: 9,  isCompleted: 1, isWarmup: 0, rpe: 8 },
      { id: '2089345678901234608', exerciseId: '2089345678901234007', exerciseName: '杠铃推举',   setNo: 4, weightKg: 24.00, reps: 8,  isCompleted: 1, isWarmup: 0, rpe: 8 },
      { id: '2089345678901234609', exerciseId: '2089345678901234002', exerciseName: '哑铃飞鸟',   setNo: 1, weightKg: 14.00, reps: 12, isCompleted: 1, isWarmup: 0, rpe: 7 },
      { id: '2089345678901234610', exerciseId: '2089345678901234002', exerciseName: '哑铃飞鸟',   setNo: 2, weightKg: 14.00, reps: 12, isCompleted: 1, isWarmup: 0, rpe: 7 },
      { id: '2089345678901234611', exerciseId: '2089345678901234002', exerciseName: '哑铃飞鸟',   setNo: 3, weightKg: 14.00, reps: 11, isCompleted: 1, isWarmup: 0, rpe: 8 },
      { id: '2089345678901234612', exerciseId: '2089345678901234011', exerciseName: '绳索夹胸',   setNo: 1, weightKg: 30.00, reps: 15, isCompleted: 1, isWarmup: 0, rpe: 7 },
      { id: '2089345678901234613', exerciseId: '2089345678901234011', exerciseName: '绳索夹胸',   setNo: 2, weightKg: 30.00, reps: 15, isCompleted: 1, isWarmup: 0, rpe: 8 },
      { id: '2089345678901234614', exerciseId: '2089345678901234011', exerciseName: '绳索夹胸',   setNo: 3, weightKg: 30.00, reps: 14, isCompleted: 1, isWarmup: 0, rpe: 8 }
    ]
  },
  {
    id: '2089345678901234502',
    userId: 'user2',
    userName: '李四',
    planId: '2089345678901234102',
    planName: '拉日A',
    recordDate: '2026-08-17',
    durationSec: 3300,
    totalVolume: 4280.00,
    totalSets: 12,
    totalReps: 80,
    note: '引体向上第一次能做 6x5，背部进步明显！',
    createdAt: '2026-08-17T19:55:00',
    sets: [
      { id: '2089345678901234620', exerciseId: '2089345678901234003', exerciseName: '引体向上', setNo: 1, weightKg: 0, reps: 6, isCompleted: 1, isWarmup: 1, rpe: 8 },
      { id: '2089345678901234621', exerciseId: '2089345678901234003', exerciseName: '引体向上', setNo: 2, weightKg: 0, reps: 6, isCompleted: 1, isWarmup: 0, rpe: 8 },
      { id: '2089345678901234622', exerciseId: '2089345678901234003', exerciseName: '引体向上', setNo: 3, weightKg: 0, reps: 5, isCompleted: 1, isWarmup: 0, rpe: 9 },
      { id: '2089345678901234623', exerciseId: '2089345678901234003', exerciseName: '引体向上', setNo: 4, weightKg: 0, reps: 5, isCompleted: 1, isWarmup: 0, rpe: 9 },
      { id: '2089345678901234624', exerciseId: '2089345678901234003', exerciseName: '引体向上', setNo: 5, weightKg: 0, reps: 4, isCompleted: 1, isWarmup: 0, rpe: 9 },
      { id: '2089345678901234625', exerciseId: '2089345678901234004', exerciseName: '坐姿划船',   setNo: 1, weightKg: 50.00, reps: 10, isCompleted: 1, isWarmup: 0, rpe: 6 },
      { id: '2089345678901234626', exerciseId: '2089345678901234004', exerciseName: '坐姿划船',   setNo: 2, weightKg: 60.00, reps: 8,  isCompleted: 1, isWarmup: 0, rpe: 7 },
      { id: '2089345678901234627', exerciseId: '2089345678901234004', exerciseName: '坐姿划船',   setNo: 3, weightKg: 60.00, reps: 8,  isCompleted: 1, isWarmup: 0, rpe: 8 },
      { id: '2089345678901234628', exerciseId: '2089345678901234004', exerciseName: '坐姿划船',   setNo: 4, weightKg: 65.00, reps: 7,  isCompleted: 1, isWarmup: 0, rpe: 8 },
      { id: '2089345678901234629', exerciseId: '2089345678901234009', exerciseName: '杠铃弯举',   setNo: 1, weightKg: 20.00, reps: 12, isCompleted: 1, isWarmup: 0, rpe: 7 },
      { id: '2089345678901234630', exerciseId: '2089345678901234009', exerciseName: '杠铃弯举',   setNo: 2, weightKg: 20.00, reps: 12, isCompleted: 1, isWarmup: 0, rpe: 7 },
      { id: '2089345678901234631', exerciseId: '2089345678901234009', exerciseName: '杠铃弯举',   setNo: 3, weightKg: 22.00, reps: 10, isCompleted: 1, isWarmup: 0, rpe: 8 }
    ]
  },
  {
    id: '2089345678901234503',
    userId: 'user3',
    userName: '王五',
    planId: '2089345678901234103',
    planName: '腿日A',
    recordDate: '2026-08-15',
    durationSec: 4200,
    totalVolume: 7950.00,
    totalSets: 15,
    totalReps: 70,
    note: '深蹲100kg x 5 完成，PR！',
    createdAt: '2026-08-15T10:10:00',
    sets: [
      { id: '2089345678901234640', exerciseId: '2089345678901234005', exerciseName: '杠铃深蹲', setNo: 1, weightKg: 60.00,  reps: 8, isCompleted: 1, isWarmup: 1, rpe: 5 },
      { id: '2089345678901234641', exerciseId: '2089345678901234005', exerciseName: '杠铃深蹲', setNo: 2, weightKg: 80.00,  reps: 5, isCompleted: 1, isWarmup: 0, rpe: 6 },
      { id: '2089345678901234642', exerciseId: '2089345678901234005', exerciseName: '杠铃深蹲', setNo: 3, weightKg: 90.00,  reps: 5, isCompleted: 1, isWarmup: 0, rpe: 7 },
      { id: '2089345678901234643', exerciseId: '2089345678901234005', exerciseName: '杠铃深蹲', setNo: 4, weightKg: 100.00, reps: 5, isCompleted: 1, isWarmup: 0, rpe: 9 },
      { id: '2089345678901234644', exerciseId: '2089345678901234005', exerciseName: '杠铃深蹲', setNo: 5, weightKg: 100.00, reps: 5, isCompleted: 1, isWarmup: 0, rpe: 9 },
      { id: '2089345678901234645', exerciseId: '2089345678901234006', exerciseName: '腿举',     setNo: 1, weightKg: 120.00, reps: 10, isCompleted: 1, isWarmup: 0, rpe: 5 },
      { id: '2089345678901234646', exerciseId: '2089345678901234006', exerciseName: '腿举',     setNo: 2, weightKg: 140.00, reps: 8,  isCompleted: 1, isWarmup: 0, rpe: 7 },
      { id: '2089345678901234647', exerciseId: '2089345678901234006', exerciseName: '腿举',     setNo: 3, weightKg: 160.00, reps: 8,  isCompleted: 1, isWarmup: 0, rpe: 8 },
      { id: '2089345678901234648', exerciseId: '2089345678901234006', exerciseName: '腿举',     setNo: 4, weightKg: 160.00, reps: 7,  isCompleted: 1, isWarmup: 0, rpe: 8 },
      { id: '2089345678901234649', exerciseId: '2089345678901234012', exerciseName: '保加利亚分腿蹲', setNo: 1, weightKg: 16.00, reps: 10, isCompleted: 1, isWarmup: 0, rpe: 6 },
      { id: '2089345678901234650', exerciseId: '2089345678901234012', exerciseName: '保加利亚分腿蹲', setNo: 2, weightKg: 16.00, reps: 10, isCompleted: 1, isWarmup: 0, rpe: 7 },
      { id: '2089345678901234651', exerciseId: '2089345678901234012', exerciseName: '保加利亚分腿蹲', setNo: 3, weightKg: 18.00, reps: 9,  isCompleted: 1, isWarmup: 0, rpe: 8 },
      { id: '2089345678901234652', exerciseId: '2089345678901234010', exerciseName: '平板支撑', setNo: 1, weightKg: 0, reps: 1, isCompleted: 1, isWarmup: 0, rpe: 7 },
      { id: '2089345678901234653', exerciseId: '2089345678901234010', exerciseName: '平板支撑', setNo: 2, weightKg: 0, reps: 1, isCompleted: 1, isWarmup: 0, rpe: 7 },
      { id: '2089345678901234654', exerciseId: '2089345678901234010', exerciseName: '平板支撑', setNo: 3, weightKg: 0, reps: 1, isCompleted: 1, isWarmup: 0, rpe: 8 }
    ]
  }
]

// ========== Mock 状态（模拟内存数据库，支持 CRUD）==========
let exercisesSeq = 120
let plansSeq     = 30
let recordsSeq   = 40

const exercisesStore = [...MOCK_EXERCISES]
const plansStore     = [...MOCK_PLANS]
const recordsStore   = [...MOCK_RECORDS]

function nextId(prefix) {
  if (prefix === 'e') return String(2089345678901234000 + (++exercisesSeq))
  if (prefix === 'p') return String(2089345678901234300 + (++plansSeq))
  return String(2089345678901234500 + (++recordsSeq))
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
    const cat = Number(params.category)
    list = list.filter(x => x.category === cat)
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
  const cat = Number(payload.category)
  const diff = Number(payload.difficulty)
  const catOpt = CATEGORY_OPTIONS.find(x => x.value === cat)
  const diffOpt = DIFFICULTY_OPTIONS.find(x => x.value === diff)
  const record = {
    id: nextId('e'),
    name: payload.name,
    category: cat,
    categoryLabel: catOpt?.label || '',
    difficulty: diff,
    difficultyLabel: diffOpt?.label || '',
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
  const cat = Number(payload.category)
  const diff = Number(payload.difficulty)
  const catOpt = CATEGORY_OPTIONS.find(x => x.value === cat)
  const diffOpt = DIFFICULTY_OPTIONS.find(x => x.value === diff)
  exercisesStore[idx] = {
    ...exercisesStore[idx],
    name: payload.name,
    category: cat,
    categoryLabel: catOpt?.label || exercisesStore[idx].categoryLabel || '',
    difficulty: diff,
    difficultyLabel: diffOpt?.label || exercisesStore[idx].difficultyLabel || '',
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
  const usedInPlan = plansStore.some(p => p.exercises && p.exercises.some(i => i.exerciseId === id))
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
    planType: p.planType,
    planTypeLabel: p.planTypeLabel,
    status: p.status,
    statusText: p.statusText,
    exerciseCount: p.exerciseCount,
    userId: p.userId,
    userName: p.userName,
    estimatedMin: p.estimatedMin,
    startedAt: p.startedAt,
    completedAt: p.completedAt,
    actualDurationSec: p.actualDurationSec,
    createdAt: p.createdAt
  }))
  if (params.keyword) {
    const kw = String(params.keyword).toLowerCase()
    list = list.filter(x =>
      x.name?.toLowerCase().includes(kw) ||
      x.description?.toLowerCase().includes(kw)
    )
  }
  if (params.status !== undefined && params.status !== null && params.status !== '') {
    list = list.filter(x => x.status === Number(params.status))
  }
  if (params.planType !== undefined && params.planType !== null && params.planType !== '') {
    list = list.filter(x => x.planType === Number(params.planType))
  }
  if (params.userId) {
    list = list.filter(x => x.userId === params.userId)
  }
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
    planType: p.planType,
    planTypeLabel: p.planTypeLabel,
    status: p.status,
    statusText: p.statusText,
    userId: p.userId,
    userName: p.userName,
    estimatedMin: p.estimatedMin,
    startedAt: p.startedAt,
    completedAt: p.completedAt,
    actualDurationSec: p.actualDurationSec,
    exercises: (p.exercises || []).map(i => ({ ...i }))
  }
}

export async function createPlan(payload) {
  await delay()
  const dup = plansStore.find(x => x.name === payload.name)
  if (dup) throw new Error('PLAN_NAME_DUPLICATED')
  const src = payload.exercises || payload.items || []
  if (src.length === 0) throw new Error('PLAN_EXERCISE_EMPTY')
  const id = nextId('p')
  const planType = Number(payload.planType) || 1
  const ptOpt = PLAN_TYPE_OPTIONS.find(x => x.value === planType)
  const now = new Date().toISOString().slice(0, 19)
  const record = {
    id,
    name: payload.name,
    description: payload.description || '',
    planType,
    planTypeLabel: ptOpt?.label || '力量',
    status: 0,
    statusText: '草稿',
    estimatedMin: payload.estimatedMin || 60,
    userId: payload.userId || 'admin',
    userName: payload.userName || '管理员',
    createdAt: now,
    startedAt: null,
    completedAt: null,
    actualDurationSec: null,
    exercises: src.map((ex, idx) => ({
      id: ex.id || String(idx + 1),
      planId: id,
      exerciseId: ex.exerciseId,
      exerciseName: exercisesStore.find(e => e.id === ex.exerciseId)?.name || '',
      sortOrder: ex.sortOrder || (idx + 1),
      targetSets: Number(ex.targetSets) || 3,
      targetReps: String(ex.targetReps ?? '10'),
      targetWeightKg: Number(ex.targetWeightKg) || 0,
      restSec: Number(ex.restSec ?? ex.restSeconds) ?? 60
    }))
  }
  record.exerciseCount = record.exercises.length
  plansStore.unshift(record)
  return { id }
}

export async function updatePlan(id, payload) {
  await delay()
  const idx = plansStore.findIndex(x => x.id === id)
  if (idx < 0) throw new Error('PLAN_NOT_FOUND')
  if (plansStore[idx].status !== 0) throw new Error('PLAN_NOT_DRAFT')
  const dup = plansStore.find(x => x.name === payload.name && x.id !== id)
  if (dup) throw new Error('PLAN_NAME_DUPLICATED')
  const src = payload.exercises || payload.items || []
  if (src.length === 0) throw new Error('PLAN_EXERCISE_EMPTY')
  const planType = Number(payload.planType) || plansStore[idx].planType
  const ptOpt = PLAN_TYPE_OPTIONS.find(x => x.value === planType)
  plansStore[idx] = {
    ...plansStore[idx],
    name: payload.name,
    description: payload.description || '',
    planType,
    planTypeLabel: ptOpt?.label || plansStore[idx].planTypeLabel,
    estimatedMin: payload.estimatedMin || plansStore[idx].estimatedMin,
    exercises: src.map((ex, i) => ({
      id: ex.id || String(i + 1),
      planId: id,
      exerciseId: ex.exerciseId,
      exerciseName: exercisesStore.find(e => e.id === ex.exerciseId)?.name || '',
      sortOrder: ex.sortOrder || (i + 1),
      targetSets: Number(ex.targetSets) || 3,
      targetReps: String(ex.targetReps ?? '10'),
      targetWeightKg: Number(ex.targetWeightKg) || 0,
      restSec: Number(ex.restSec ?? ex.restSeconds) ?? 60
    }))
  }
  plansStore[idx].exerciseCount = plansStore[idx].exercises.length
  return { id }
}

export async function deletePlan(id) {
  await delay()
  const idx = plansStore.findIndex(x => x.id === id)
  if (idx < 0) throw new Error('PLAN_NOT_FOUND')
  const plan = plansStore[idx]
  if (plan.status === 1) throw new Error('PLAN_ALREADY_IN_PROGRESS')
  if (plan.status === 2) throw new Error('PLAN_ALREADY_COMPLETED')
  plansStore.splice(idx, 1)
  return null
}

// ========== 训练状态流转接口 ==========

export async function startPlan(id) {
  await delay()
  const idx = plansStore.findIndex(x => x.id === id)
  if (idx < 0) throw new Error('PLAN_NOT_FOUND')
  const plan = plansStore[idx]
  if (plan.status === 1) throw new Error('PLAN_ALREADY_IN_PROGRESS')
  if (plan.status === 2) throw new Error('PLAN_ALREADY_COMPLETED')
  plan.status = 1
  plan.statusText = '进行中'
  plan.startedAt = new Date().toISOString().slice(0, 19)
  return {
    planId: id,
    status: 1,
    statusText: '进行中',
    startedAt: plan.startedAt
  }
}

export async function completePlan(id, payload) {
  await delay()
  const idx = plansStore.findIndex(x => x.id === id)
  if (idx < 0) throw new Error('PLAN_NOT_FOUND')
  const plan = plansStore[idx]
  if (plan.status !== 1) throw new Error('PLAN_NOT_IN_PROGRESS')

  const durationSec = Number(payload.durationSec) || 0
  if (durationSec < 300) throw new Error('PLAN_DURATION_TOO_SHORT')
  if (!payload.actualSets || payload.actualSets.length === 0) throw new Error('RECORD_SET_EMPTY')

  // 按后端规则计算：isWarmup=1 的组不参与容量统计
  const totalVolume = payload.actualSets.reduce((s, x) => {
    if (Number(x.isWarmup) === 1) return s
    return s + (Number(x.weightKg) || 0) * (Number(x.reps) || 0)
  }, 0)
  const totalSets = payload.actualSets.filter(x => Number(x.isCompleted) !== 0).length
  const totalReps = payload.actualSets.reduce((s, x) =>
    Number(x.isCompleted) === 0 ? s : s + (Number(x.reps) || 0)
  , 0)
  const recordId = nextId('r')
  const today = new Date().toISOString().slice(0, 10)

  const record = {
    id: recordId,
    userId: plan.userId,
    userName: plan.userName,
    planId: id,
    planName: plan.name,
    recordDate: today,
    durationSec,
    totalVolume: Number(totalVolume.toFixed(2)),
    totalSets,
    totalReps,
    note: payload.note || '',
    createdAt: new Date().toISOString().slice(0, 19),
    sets: payload.actualSets.map((s, i) => ({
      id: String(2089345678901234700 + i),
      exerciseId: s.exerciseId,
      exerciseName: exercisesStore.find(e => e.id === s.exerciseId)?.name || '',
      setNo: Number(s.setNo) || (i + 1),
      weightKg: Number(s.weightKg) || 0,
      reps: Number(s.reps) || 0,
      isCompleted: Number(s.isCompleted) ?? 1,
      isWarmup: Number(s.isWarmup) ?? 0,
      rpe: s.rpe ?? null
    }))
  }
  recordsStore.unshift(record)

  plan.status = 2
  plan.statusText = '已完成'
  plan.completedAt = new Date().toISOString().slice(0, 19)
  plan.actualDurationSec = durationSec
  plan.startedAt = plan.startedAt || new Date().toISOString().slice(0, 19)

  return {
    recordId,
    planId: id,
    recordDate: today,
    durationSec,
    totalVolume: record.totalVolume,
    totalSets,
    totalReps
  }
}

export async function cancelPlan(id) {
  await delay()
  const idx = plansStore.findIndex(x => x.id === id)
  if (idx < 0) throw new Error('PLAN_NOT_FOUND')
  const plan = plansStore[idx]
  if (plan.status !== 1) throw new Error('PLAN_NOT_IN_PROGRESS')
  plan.status = 3
  plan.statusText = '已取消'
  plan.startedAt = null
  return {
    planId: id,
    status: 3,
    statusText: '已取消'
  }
}

export async function getInProgressPlan() {
  await delay(150)
  const plan = plansStore.find(p => p.status === 1)
  if (!plan) {
    return { hasActivePlan: false }
  }
  const elapsedSec = plan.startedAt
    ? Math.floor((Date.now() - new Date(plan.startedAt).getTime()) / 1000)
    : 0
  return {
    hasActivePlan: true,
    planId: plan.id,
    name: plan.name,
    startedAt: plan.startedAt,
    elapsedSec: Math.max(0, elapsedSec),
    plan
  }
}

// ========== 训练记录接口（只读）==========

export async function getRecordList(params = {}) {
  await delay()
  let list = recordsStore.map(r => ({
    id: r.id,
    userId: r.userId,
    userName: r.userName,
    planId: r.planId,
    planName: r.planName,
    recordDate: r.recordDate,
    durationSec: r.durationSec,
    totalVolume: r.totalVolume,
    totalSets: r.totalSets,
    totalReps: r.totalReps,
    note: r.note,
    createdAt: r.createdAt
  }))
  if (params.startDate) {
    list = list.filter(r => r.recordDate >= params.startDate)
  }
  if (params.endDate) {
    list = list.filter(r => r.recordDate <= params.endDate)
  }
  if (params.userId) {
    list = list.filter(r => r.userId === params.userId)
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
    userId: r.userId,
    userName: r.userName,
    planId: r.planId,
    planName: r.planName,
    recordDate: r.recordDate,
    durationSec: r.durationSec,
    totalVolume: r.totalVolume,
    totalSets: r.totalSets,
    totalReps: r.totalReps,
    note: r.note,
    createdAt: r.createdAt,
    sets: (r.sets || []).map(s => ({ ...s }))
  }
}

// ========== 辅助函数 ==========

// 辅助：获取所有动作（用于计划编辑页的选择器，不分页）
export async function getAllExercises() {
  await delay(120)
  return [...exercisesStore].sort((a, b) => (b.isSystem - a.isSystem) || a.name.localeCompare(b.name, 'zh-Hans'))
}

// 辅助：获取所有计划（用于训练选择，不分页）
export async function getAllPlans() {
  await delay(120)
  return plansStore
    .filter(p => p.status !== 2)
    .map(p => ({
      id: p.id,
      name: p.name,
      description: p.description,
      status: p.status,
      planType: p.planType,
      planTypeLabel: p.planTypeLabel,
      exercises: p.exercises || []
    }))
    .sort((a, b) => a.name.localeCompare(b.name, 'zh-Hans'))
}

// 辅助：获取所有用户（筛选用）
export const MOCK_USER_OPTIONS = [
  { value: 'user1', label: '张三' },
  { value: 'user2', label: '李四' },
  { value: 'user3', label: '王五' }
]
