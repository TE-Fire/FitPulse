# FitPulse 后端开发日志（进行中）

> 会话主题：Training 模块 CRUD 开发（动作库 / 训练计划 / 训练记录）
> 起始时间：2026-08-19
> 状态：进行中（会话结束后由用户确认，重命名为 `yyyyMMdd_<会话概括>.md`）

---

## 一、需求与上下文

### 1.1 用户原始需求
1. auth + user + file 模块已全部完成，下一步进入训练模块
2. 先做训练模块的 CRUD，实现流程由后端自行安排
3. 任务计划 + 前端实现说明写入本日志，供另一个前端会话参考实现
4. 前端实现说明要求：简洁但不会产生误解

### 1.2 全局规则提醒
- 每次制定任务先与用户讨论确认后执行
- 每次改动立即 git commit
- Service 层接口-impl 分离
- 每个模块独立错误枚举实现 BaseExceptionInterface
- 后端 JDK 21，API 统一前缀 `/api/v1`
- 变量声明禁用 var
- 编码中使用设计模式/高超技巧需指出便于学习

---

## 二、训练模块任务拆分

### 2.1 涉及数据表（schema.sql 已存在）

| 表 | 说明 | 关键字段 |
|---|---|---|
| `exercise` | 动作库 | id, name, category, difficulty, muscle_group, equipment, description, image_url, is_system |
| `workout_plan` | 训练计划 | id, user_id, name, description, status |
| `workout_plan_item` | 计划-动作关联 | id, plan_id, exercise_id, sort_order, target_sets, target_reps, rest_seconds |
| `workout_record` | 训练记录 | id, user_id, plan_id, record_date, duration_sec, total_volume, total_sets, total_reps, note |
| `workout_set` | 训练组明细 | id, record_id, exercise_id, set_no, weight_kg, reps, rpe, rest_seconds |

### 2.2 任务阶段

| 阶段 | 任务 | 交付物 |
|---|---|---|
| **P1** | 实体 + Mapper + 错误枚举 | 5 个实体类、5 个 Mapper、TrainingErrorCode |
| **P2** | Exercise 动作库 CRUD | DTO + Service + Controller（5 个接口） |
| **P3** | WorkoutPlan 训练计划 CRUD | DTO + Service + Controller（4 个接口，含 plan_item 关联） |
| **P4** | WorkoutRecord 训练记录 CRUD | DTO + Service + Controller（3 个接口，含 set 明细 + 容量回写） |
| **P5** | 接口文档同步 + 预置动作库 | 更新 接口文档.md / .json，预置 10 个系统级动作 |

---

## 三、前端实现说明（供前端会话参考）

> 以下内容为前端开发者必须了解的接口契约与交互逻辑，按模块组织。

### 3.1 动作库管理（Exercise）

#### 接口清单

| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| GET | `/api/v1/training/exercises` | 分页查询动作列表 | 需登录 |
| GET | `/api/v1/training/exercises/{id}` | 查询动作详情 | 需登录 |
| POST | `/api/v1/training/exercises` | 新建动作 | 需登录 |
| PUT | `/api/v1/training/exercises/{id}` | 修改动作 | 需登录 |
| DELETE | `/api/v1/training/exercises/{id}` | 删除动作 | 需登录 |

#### 列表查询参数（Query String）

```
GET /api/v1/training/exercises?page=1&size=10&name=卧推&category=chest&difficulty=2
```

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页条数，默认 10 |
| name | string | 否 | 动作名称模糊搜索 |
| category | string | 否 | 分类：chest/back/leg/shoulder/arm/core/cardio |
| difficulty | int | 否 | 难度：1=入门 2=进阶 3=达人 |

#### 列表响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": "1893456789012345678",
        "name": "杠铃卧推",
        "category": "chest",
        "difficulty": 2,
        "muscleGroup": "胸大肌",
        "equipment": "barbell",
        "description": "平躺在卧推凳上，双手握杠铃下放至胸部再推起",
        "imageUrl": null,
        "isSystem": true
      }
    ],
    "total": 15,
    "page": 1,
    "size": 10
  }
}
```

#### 新建/修改请求体

```json
{
  "name": "杠铃卧推",
  "category": "chest",
  "difficulty": 2,
  "muscleGroup": "胸大肌",
  "equipment": "barbell",
  "description": "平躺在卧推凳上...",
  "imageUrl": "https://..."
}
```

**字段约束**：
- `name`：1-50 字符，必填
- `category`：枚举值，必填
- `difficulty`：1/2/3，必填
- `muscleGroup`/`equipment`/`description`/`imageUrl`：选填
- `isSystem`：**后端自动设置**，前端无需传（系统预置=1，用户新建=0）

#### 删除规则
- **系统预置动作（is_system=1）不允许删除**，返回错误码 409
- 用户自建动作（is_system=0）可删除（逻辑删除）

#### 前端页面建议
- 表格展示列表，支持分页 + 名称搜索 + 分类/难度筛选
- 新增/编辑用 Dialog 表单
- 系统动作的"删除"按钮置灰或隐藏

---

### 3.2 训练计划管理（WorkoutPlan）

#### 接口清单

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/v1/training/plans` | 当前用户的计划列表（分页） |
| GET | `/api/v1/training/plans/{id}` | 计划详情（含关联动作列表） |
| POST | `/api/v1/training/plans` | 新建计划（同时提交关联动作） |
| PUT | `/api/v1/training/plans/{id}` | 修改计划（全量替换关联动作） |
| DELETE | `/api/v1/training/plans/{id}` | 删除计划 |

#### 列表响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": "1893456789012345678",
        "name": "推日A",
        "description": "胸+三头",
        "status": 1,
        "itemCount": 5,
        "createdAt": "2026-08-19T10:30:00"
      }
    ],
    "total": 3,
    "page": 1,
    "size": 10
  }
}
```

#### 计划详情响应（含关联动作）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "1893456789012345678",
    "name": "推日A",
    "description": "胸+三头",
    "status": 1,
    "items": [
      {
        "id": "1893456789012345679",
        "exerciseId": "1893456789012345001",
        "exerciseName": "杠铃卧推",
        "sortOrder": 1,
        "targetSets": 4,
        "targetReps": 8,
        "restSeconds": 90
      },
      {
        "id": "1893456789012345680",
        "exerciseId": "1893456789012345002",
        "exerciseName": "哑铃飞鸟",
        "sortOrder": 2,
        "targetSets": 3,
        "targetReps": 12,
        "restSeconds": 60
      }
    ]
  }
}
```

#### 新建/修改请求体

```json
{
  "name": "推日A",
  "description": "胸+三头",
  "items": [
    {
      "exerciseId": "1893456789012345001",
      "sortOrder": 1,
      "targetSets": 4,
      "targetReps": 8,
      "restSeconds": 90
    },
    {
      "exerciseId": "1893456789012345002",
      "sortOrder": 2,
      "targetSets": 3,
      "targetReps": 12,
      "restSeconds": 60
    }
  ]
}
```

**字段约束**：
- `name`：1-50 字符，必填
- `items`：至少 1 个，每个 item 的 `exerciseId` 必须存在
- `sortOrder`：正整数，前端拖拽排序后传给后端
- `targetSets`/`targetReps`：1-99，必填
- `restSeconds`：0-600，选填

#### 修改语义
- **全量替换**：修改时后端先删除该计划的所有 item，再按新提交的 items 插入
- 前端无需做"增量 diff"，直接提交完整列表即可

#### 前端页面建议
- 计划列表页：卡片或表格展示
- 计划编辑页：左侧动作库选择器，右侧已选动作列表（可拖拽排序）
- 拖拽排序后更新 sortOrder 再提交

---

### 3.3 训练记录管理（WorkoutRecord）

#### 接口清单

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/v1/training/records` | 当前用户的训练记录列表（分页+日期筛选） |
| GET | `/api/v1/training/records/{id}` | 记录详情（含每组明细） |
| POST | `/api/v1/training/records` | 提交训练记录（含多组明细） |

#### 列表查询参数

```
GET /api/v1/training/records?page=1&size=10&startDate=2026-08-01&endDate=2026-08-19
```

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| page | int | 否 | 默认 1 |
| size | int | 否 | 默认 10 |
| startDate | string | 否 | 起始日期 yyyy-MM-dd |
| endDate | string | 否 | 结束日期 yyyy-MM-dd |

#### 列表响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": "1893456789012345678",
        "planId": "1893456789012345679",
        "planName": "推日A",
        "recordDate": "2026-08-19",
        "durationSec": 3600,
        "totalVolume": 5400.0,
        "totalSets": 12,
        "totalReps": 96,
        "note": "状态不错"
      }
    ],
    "total": 5,
    "page": 1,
    "size": 10
  }
}
```

#### 记录详情响应（含每组明细）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "1893456789012345678",
    "planId": "1893456789012345679",
    "planName": "推日A",
    "recordDate": "2026-08-19",
    "durationSec": 3600,
    "totalVolume": 5400.0,
    "totalSets": 12,
    "totalReps": 96,
    "note": "状态不错",
    "sets": [
      {
        "id": "1893456789012345681",
        "exerciseId": "1893456789012345001",
        "exerciseName": "杠铃卧推",
        "setNo": 1,
        "weightKg": 60.0,
        "reps": 8,
        "rpe": 7,
        "restSeconds": 90
      },
      {
        "id": "1893456789012345682",
        "exerciseId": "1893456789012345001",
        "exerciseName": "杠铃卧推",
        "setNo": 2,
        "weightKg": 65.0,
        "reps": 8,
        "rpe": 8,
        "restSeconds": 120
      }
    ]
  }
}
```

#### 提交训练记录请求体

```json
{
  "planId": "1893456789012345679",
  "recordDate": "2026-08-19",
  "durationSec": 3600,
  "note": "状态不错",
  "sets": [
    {
      "exerciseId": "1893456789012345001",
      "setNo": 1,
      "weightKg": 60.0,
      "reps": 8,
      "rpe": 7,
      "restSeconds": 90
    },
    {
      "exerciseId": "1893456789012345001",
      "setNo": 2,
      "weightKg": 65.0,
      "reps": 8,
      "rpe": 8,
      "restSeconds": 120
    }
  ]
}
```

**字段约束**：
- `planId`：选填（自由训练可不关联计划）
- `recordDate`：必填，格式 yyyy-MM-dd
- `durationSec`：选填，训练时长（秒）
- `sets`：至少 1 组
  - `exerciseId`：必填
  - `setNo`：正整数，同一动作内递增
  - `weightKg`：0-999，必填
  - `reps`：1-999，必填
  - `rpe`：1-10，选填（主观疲劳度）
  - `restSeconds`：0-600，选填

#### 容量自动计算（后端自动完成，前端无需传）

提交训练记录时，后端会自动计算并回写以下字段：

```
total_volume = Σ(set.weightKg × set.reps)   // 例如 60×8 + 65×8 = 1000
total_sets   = count(sets)                    // 例如 2
total_reps   = Σ(set.reps)                    // 例如 8 + 8 = 16
```

**前端无需传 totalVolume / totalSets / totalReps**，后端自动计算后返回。

#### 前端页面建议
- 记录列表页：按日期倒序，支持日期范围筛选
- 记录详情页：Drawer 展示，按动作分组显示每组明细
- 记录录入页：
  - 选择训练计划（可选）→ 自动带入计划中的动作和目标组数
  - 每组录入：重量 × 次数 + RPE（滑块或数字输入）
  - 支持"复制上一组"按钮快速填充
  - 实时显示已累计的容量（前端可本地计算预览）

---

## 四、后端实现要点（供后端开发参考）

### 4.1 实体类清单

| 实体 | 表 | 位置 |
|---|---|---|
| Exercise | exercise | entity/ |
| WorkoutPlan | workout_plan | entity/ |
| WorkoutPlanItem | workout_plan_item | entity/ |
| WorkoutRecord | workout_record | entity/ |
| WorkoutSet | workout_set | entity/ |

### 4.2 设计模式预告

1. **批量操作**：WorkoutPlanItem 的插入/删除使用 MyBatis-Plus 的 `saveBatch` / `removeByIds`
2. **事务管理**：WorkoutRecord 提交时使用 `@Transactional` 保证 record + set 的原子性
3. **计算逻辑抽取**：容量计算逻辑抽取为私有方法，便于复用和测试
4. **投影模式**：列表查询使用自定义 SQL 只取需要的字段，避免全字段查询

### 4.3 错误枚举（TrainingErrorCode）

| 枚举值 | 错误码 | 说明 |
|---|---|---|
| EXERCISE_NOT_FOUND | 404 | 动作不存在 |
| EXERCISE_SYSTEM_CANNOT_DELETE | 409 | 系统预置动作不可删除 |
| EXERCISE_NAME_DUPLICATED | 409 | 动作名称重复 |
| PLAN_NOT_FOUND | 404 | 计划不存在 |
| PLAN_NAME_DUPLICATED | 409 | 计划名称重复 |
| PLAN_ITEM_EMPTY | 400 | 计划至少包含 1 个动作 |
| RECORD_NOT_FOUND | 404 | 训练记录不存在 |
| RECORD_SET_EMPTY | 400 | 训练记录至少包含 1 组 |
| EXERCISE_IN_USE | 409 | 动作被计划/记录引用，不可删除 |

---

## 五、前端实现进度（2026-08-21）

### 5.1 技术栈
- Vue 3 + Vite + Element Plus + Pinia + Vue Router + Axios + Tailwind CSS
- ECharts（数据看板图表）

### 5.2 已完成页面

| 页面 | 路由 | 说明 | 状态 |
|---|---|---|---|
| 动作库管理 | `/training/exercises` | 列表+筛选+新增/编辑 Dialog | ✅ 完成 |
| 训练计划列表 | `/training/plans` | 卡片列表+查看/编辑/删除 | ✅ 完成 |
| 训练计划编辑 | `/training/plans/edit/:id?` | 左右分栏：动作选择器+编排编辑 | ✅ 完成 |
| 训练记录列表 | `/training/records` | 列表+日期筛选+详情 Drawer | ✅ 完成 |
| 训练记录录入 | `/training/records/create` | 选计划/自由动作+分组录入 | ✅ 完成 |

### 5.3 Mock 数据
- **文件**：`src/mock/training.js`
- **内容**：1380 行静态数据，包含枚举常量、系统预置动作（约 15 个）、训练计划示例、训练记录示例
- **CRUD 模拟**：所有增删改查操作通过 Promise 模拟异步返回

### 5.4 API 层
- **文件**：`src/api/training.js`（440 行）
- **模式**：`FORCE_MOCK=true`，当前强制走前端 Mock
- **切换后端**：设置 `FORCE_MOCK=false` 即可切换到真实后端接口

### 5.5 UI 设计风格
- **整体风格**：极简清新健康风 + 数据看板风
- **配色**：浅色背景、白卡、紫青品牌色点缀（#667eea、#764ba2）
- **特点**：
  - Hero 区域展示统计数据渐变背景
  - 卡片式布局，圆角 16-20px
  - Dialog 表单分组设计，带装饰性色条
  - 自定义难度选择器（卡片式）
  - 输入框聚焦时带阴影环效果

### 5.6 构建验证
- `npm run build` ✅ 成功
- 输出产物：约 30 个 chunk，总 JS 约 400KB (gzip)

### 5.7 Git 提交记录
```
feat: training module prototype with mock data
- 5 个页面组件
- API 层 + Mock 数据
- 路由配置 + 菜单入口
refactor: redesign exercise form dialog with grouped card layout
- 分组卡片式表单布局
- 自定义难度选择器
- 统一输入框/按钮样式
```

### 5.8 后端对接说明
当前训练模块使用前端 Mock 数据作为原型，后端接口完成后：

1. 修改 `src/api/training.js` 中 `FORCE_MOCK=false`
2. 前端页面无需改动，API 层会自动切换到真实接口
3. 接口路径已按后端规范预定义（`/api/v1/training/exercises` 等）
