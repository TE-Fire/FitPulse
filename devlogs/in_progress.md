# FitPulse 后端开发日志（进行中）

> 会话主题：Training 模块 CRUD 开发（动作库 / 训练计划 / 训练记录）
> 起始时间：2026-08-21
> 状态：进行中（会话结束后由用户确认，重命名为 `yyyyMMdd_<会话概括>.md`）

---

## 一、需求与上下文

### 1.1 用户原始需求
1. auth + user + file 模块已全部完成，下一步进入训练模块
2. 先做训练模块的 CRUD，实现流程由后端自行安排
3. 任务计划 + 前端实现说明写入本日志，供另一个前端会话参考实现
4. 前端实现说明要求：简洁但不会产生误解
5. **文档中的表字段必须与交付方案严格一致**

### 1.2 关键业务修订（2026-08-21 讨论确认）

**核心变更**：训练记录不再手动独立提交，必须来源于训练计划的完整执行过程。

```
原方案：用户手动 POST /training/records 提交记录（planId 可选）
新方案：选择计划 → start → 前端本地计时器（实时显示）→ complete（校验≥5min）→ 系统自动生成 record
```

**关键规则**：
1. 必须选择计划才能开始训练，记录只在完成训练时系统自动生成
2. 前端本地计时器（可暂停/继续），完成时二次确认弹窗
3. 训练时长 < 5 分钟，拒绝生成记录
4. 已完成的计划可再次开始：通过 copy 接口复制新计划后 start
5. 组明细可超出 plan_exercise 模板（plan 只是参考初始化）

### 1.3 全局规则提醒
- 每次制定任务先与用户讨论确认后执行
- 每次改动立即 git commit
- Service 层接口-impl 分离
- 每个模块独立错误枚举实现 BaseExceptionInterface
- 后端 JDK 21，API 统一前缀 `/api/v1`
- 变量声明禁用 var
- 编码中使用设计模式/高超技巧需指出便于学习

---

## 二、数据库变更（V3 迁移）

### 2.1 迁移文件
- **路径**：`fitness-backend/src/main/resources/sql/migration/V3__training_extend_and_seed.sql`
- **执行前需先执行 V1(schema.sql) + V2**

### 2.2 V3 追加字段（对齐 schema.sql 真实结构，不改动已有字段）

| 表 | 追加字段 | 类型 | 说明 |
|---|---|---|---|
| **exercise** | `muscle_group` | VARCHAR(128) NULL | 目标肌群（原 schema 无此字段，动作详情分类展示用） |
| **workout_plan** | `status` | TINYINT DEFAULT 0 | 0=DRAFT 1=IN_PROGRESS 2=COMPLETED 3=CANCELLED（原 schema 无此字段） |
| | `started_at` | DATETIME NULL | start 接口写入 |
| | `completed_at` | DATETIME NULL | complete 接口写入 |
| | `actual_duration_sec` | INT NULL | 前端累计的实际训练秒数 |
| **workout_plan_exercise** | `target_weight_kg` | DECIMAL(8,2) NULL | 计划建议默认重量（录入训练时快速填充） |

### 2.3 schema.sql 已有字段（**实体/DTO 严格按此**）

#### exercise 表
```
id BIGINT PK
name VARCHAR(128) NOT NULL
category TINYINT NOT NULL        -- 1=胸 2=背 3=肩 4=手臂 5=腿 6=核心 7=有氧 8=全身（！数字枚举，非字符串）
difficulty TINYINT DEFAULT 2     -- 1=入门 2=中级 3=高级
equipment VARCHAR(64) NULL
muscle_group VARCHAR(128) NULL   -- V3 追加
description TEXT NULL
image_url VARCHAR(512) NULL
is_system TINYINT DEFAULT 1      -- 1=系统预置 0=自定义
user_id BIGINT NULL              -- 自定义动作的归属用户（0 自定义）
created_at / updated_at / deleted
```

#### workout_plan 表
```
id BIGINT PK
user_id BIGINT NOT NULL
name VARCHAR(128) NOT NULL
plan_type TINYINT DEFAULT 1     -- 1=力量 2=有氧 3=混合
description VARCHAR(512) NULL
estimated_min INT NULL          -- 预估时长分钟
created_at / updated_at
status TINYINT DEFAULT 0        -- V3 追加 0/1/2/3
started_at DATETIME NULL        -- V3 追加
completed_at DATETIME NULL      -- V3 追加
actual_duration_sec INT NULL    -- V3 追加
deleted TINYINT DEFAULT 0
```

#### workout_plan_exercise 表（！表名，不是 workout_plan_item）
```
id BIGINT PK
plan_id BIGINT NOT NULL
exercise_id BIGINT NOT NULL
sort_order INT DEFAULT 0
target_sets TINYINT DEFAULT 3
target_reps VARCHAR(32) NULL    -- ! 字符串！如 "8-12" / "力竭" / "12,10,8"
rest_sec INT NULL               -- ! 字段名 rest_sec，不是 rest_seconds
target_weight_kg DECIMAL(8,2)   -- V3 追加（建议默认重量）
created_at
```

#### workout_record 表
```
id BIGINT PK
user_id BIGINT NOT NULL
plan_id BIGINT NULL
record_date DATE NOT NULL
duration_sec INT NULL
total_volume DECIMAL(12,2) DEFAULT 0
total_sets INT DEFAULT 0
total_reps INT DEFAULT 0
note VARCHAR(1024) NULL
created_at / updated_at / deleted
```

#### workout_set 表
```
id BIGINT PK
record_id BIGINT NOT NULL
exercise_id BIGINT NOT NULL
set_no TINYINT NOT NULL         -- 第N组
weight_kg DECIMAL(8,2) NULL
reps INT NULL
is_completed TINYINT DEFAULT 1 -- ! 1=完成 0=未完成（schema 已有）
is_warmup TINYINT DEFAULT 0    -- ! 1=热身组（schema 已有）
rpe TINYINT NULL               -- 1-10
created_at
-- 注意：workout_set 表中没有 rest_sec 字段（与 plan_exercise 不同）！
```

### 2.4 预置系统级动作（exercise.is_system=1，共 10 个）
category 对齐 schema 数字枚举：
1. 杠铃卧推 胸=1 难度=2
2. 哑铃飞鸟 胸=1 难度=1
3. 杠铃深蹲 腿=5 难度=3
4. 腿举 腿=5 难度=1
5. 引体向上 背=2 难度=3
6. 坐姿划船 背=2 难度=1
7. 杠铃推举 肩=3 难度=2
8. 哑铃侧平举 肩=3 难度=1
9. 杠铃弯举 手臂=4 难度=1
10. 平板支撑 核心=6 难度=1

### 2.5 status 枚举（4 态）

| 值 | 枚举 | 说明 |
|---|---|---|
| 0 | DRAFT | 草稿，可编辑/可开始 |
| 1 | IN_PROGRESS | 训练中（前端计时器运行） |
| 2 | COMPLETED | 已完成（已生成 record，不可再开始） |
| 3 | CANCELLED | 已取消（可再次 start，无需 copy） |

---

## 三、训练模块任务拆分

### 3.1 涉及数据表

| 表 | 主键类型 | 核心字段 |
|---|---|---|
| `exercise` | 雪花 | category(TINYINT), difficulty, is_system, user_id, muscle_group(V3) |
| `workout_plan` | 雪花 | plan_type(1力量2有氧3混合), status(V3), started/completed/actual_duration(V3) |
| `workout_plan_exercise` | 雪花 | target_reps(VARCHAR), rest_sec, target_weight_kg(V3) |
| `workout_record` | 雪花 | record_date(DATE), duration_sec, total_volume/sets/reps |
| `workout_set` | 雪花 | is_completed, is_warmup, rpe, weight_kg(DECIMAL8,2) |

### 3.2 任务阶段

| 阶段 | 任务 | 交付物数 |
|---|---|---|
| **P1** | 实体 + Mapper + 错误枚举 | 5 实体 + 5 Mapper + 1 ErrorCode（16枚举） |
| **P2** | Exercise 动作库 CRUD（5 接口） | CreateReq/UpdateReq/VO + Service + Controller |
| **P3** | WorkoutPlan 训练计划（10 接口） | CRUD + start/complete/cancel/copy/in-progress + plan_exercise 全量替换 + @Transactional 三表写入 |
| **P4** | WorkoutRecord 查询（2 接口） | 列表 + 详情（删除 POST /training/records） |
| **P5** | 接口文档同步 + V3 SQL 验证 | 接口文档.md / .json + SQL 数据正确性检查 |

---

## 四、前端实现说明（供前端会话参考，字段类型严格对齐）

### 4.1 动作库管理（Exercise）

#### category 数字枚举映射（前端必须转中文展示）

```javascript
const CATEGORY_MAP = {
  1: { label: '胸', color: 'danger' },
  2: { label: '背', color: 'primary' },
  3: { label: '肩', color: 'warning' },
  4: { label: '手臂', color: 'success' },
  5: { label: '腿', color: 'info' },
  6: { label: '核心', color: 'purple' },
  7: { label: '有氧', color: 'cyan' },
  8: { label: '全身', color: '' }
}
const DIFFICULTY_MAP = {
  1: { label: '入门', tag: 'success' },
  2: { label: '中级', tag: 'warning' },
  3: { label: '高级', tag: 'danger' }
}
```

#### 接口清单

| 方法 | 路径 | 鉴权 |
|---|---|---|
| GET | `/api/v1/training/exercises` | 需登录 |
| GET | `/api/v1/training/exercises/{id}` | 需登录 |
| POST | `/api/v1/training/exercises` | 需登录（is_system=0, user_id=当前用户） |
| PUT | `/api/v1/training/exercises/{id}` | 需登录（仅 user_id=自己 或 is_system=0 允许） |
| DELETE | `/api/v1/training/exercises/{id}` | 需登录（is_system=1 拒绝 409） |

#### 列表查询参数

```
GET /api/v1/training/exercises?page=1&size=10&name=卧推&category=1&difficulty=2
```

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| page | int | 否 | 默认 1 |
| size | int | 否 | 默认 10 |
| name | string | 否 | 模糊搜索 name |
| category | int | 否 | 1-8 数字枚举（！不是 chest/back 字符串） |
| difficulty | int | 否 | 1-3 |

#### 列表响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": "2089345678901234001",
        "name": "杠铃卧推",
        "category": 1,
        "categoryLabel": "胸",
        "difficulty": 2,
        "difficultyLabel": "中级",
        "equipment": "杠铃",
        "muscleGroup": "胸大肌、肱三头肌",
        "imageUrl": null,
        "isSystem": true,
        "isMine": false
      }
    ],
    "total": 10,
    "page": 1,
    "size": 10
  }
}
```

**字段说明（前端必看）**：
- `category` / `difficulty`：数字枚举，前端映射中文颜色
- `categoryLabel` / `difficultyLabel`：后端返回中文标签，前端直接展示
- `isSystem`：true=系统预置，删除按钮禁用；false=用户自建
- `isMine`：true=当前用户自建，允许编辑/删除

#### 新建/修改请求体

```json
{
  "name": "哈克深蹲",
  "category": 5,
  "difficulty": 1,
  "equipment": "器械",
  "muscleGroup": "股四头肌、臀大肌",
  "description": "站在哈克深蹲机上...",
  "imageUrl": null
}
```

**字段约束**：
- `name`：1-128 必填
- `category`：1-8 数字 必填
- `difficulty`：1-3 必填
- `equipment`/`muscleGroup`/`description`/`imageUrl`：选填
- **isSystem / user_id：后端自动填充，前端不传**（系统动作=1 0归属，自定义=0 当前归属）

---

### 4.2 训练计划管理（WorkoutPlan）

#### plan_type 枚举

```javascript
const PLAN_TYPE_MAP = {
  1: { label: '力量', color: 'primary' },
  2: { label: '有氧', color: 'success' },
  3: { label: '混合', color: 'warning' }
}
```

#### status 枚举（后端同时返回 statusText）

```javascript
const STATUS_MAP = {
  0: { label: '草稿', type: 'info' },
  1: { label: '进行中', type: 'primary' },
  2: { label: '已完成', type: 'success' },
  3: { label: '已取消', type: 'default' }
}
```

#### 接口清单

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/v1/training/plans` | 当前用户计划列表（分页） |
| GET | `/api/v1/training/plans/{id}` | 详情（含关联动作 workout_plan_exercise 列表） |
| POST | `/api/v1/training/plans` | 新建（同时提交关联动作） |
| PUT | `/api/v1/training/plans/{id}` | 修改（仅 status=DRAFT 允许，全量替换 exercises） |
| DELETE | `/api/v1/training/plans/{id}` | 删除计划 |
| POST | `/training/plans/{id}/start` | 开始训练（status→IN_PROGRESS，写 started_at） |
| POST | `/training/plans/{id}/complete` | 完成训练（校验≥5min，生成 record+sets） |
| POST | `/training/plans/{id}/cancel` | 放弃训练（status→CANCELLED，不生成记录） |
| POST | `/training/plans/{id}/copy` | 复制计划（深拷贝+exercises，新 status=DRAFT） |
| GET | `/training/plans/in-progress` | 当前进行中的训练（恢复计时器用） |

#### 列表响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": "2089345678901234101",
        "name": "推日A",
        "planType": 1,
        "planTypeLabel": "力量",
        "status": 0,
        "statusText": "草稿",
        "description": "胸+三头",
        "estimatedMin": 60,
        "exerciseCount": 5,
        "startedAt": null,
        "completedAt": null,
        "actualDurationSec": null,
        "createdAt": "2026-08-21T10:30:00"
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
    "id": "2089345678901234101",
    "name": "推日A",
    "planType": 1,
    "planTypeLabel": "力量",
    "status": 0,
    "statusText": "草稿",
    "description": "胸+三头",
    "estimatedMin": 60,
    "exercises": [
      {
        "id": "2089345678901234201",
        "planId": "2089345678901234101",
        "exerciseId": "2089345678901234001",
        "exerciseName": "杠铃卧推",
        "sortOrder": 1,
        "targetSets": 4,
        "targetReps": "8-12",
        "targetWeightKg": 60.00,
        "restSec": 90
      },
      {
        "id": "2089345678901234202",
        "planId": "2089345678901234101",
        "exerciseId": "2089345678901234002",
        "exerciseName": "哑铃飞鸟",
        "sortOrder": 2,
        "targetSets": 3,
        "targetReps": "12",
        "targetWeightKg": 20.00,
        "restSec": 60
      }
    ]
  }
}
```

**前端注意**：
- `targetReps`：**字符串**，可能是 "8-12" "12,10,8" "力竭"
- `restSec`：字段名是 **restSec**（单数），不是 restSeconds
- `targetWeightKg`：V3 新增的默认建议重量，前端录入训练时可以一键带入

#### 新建/修改请求体

```json
{
  "name": "推日A",
  "planType": 1,
  "description": "胸+三头",
  "estimatedMin": 60,
  "exercises": [
    {
      "exerciseId": "2089345678901234001",
      "sortOrder": 1,
      "targetSets": 4,
      "targetReps": "8-12",
      "targetWeightKg": 60.0,
      "restSec": 90
    }
  ]
}
```

**字段约束**：
- `name`：1-128 必填
- `planType`：1/2/3 必填（！不是字符串）
- `exercises`：至少 1 个，exerciseId 必须存在
- `sortOrder`：正整数，拖拽排序后更新
- `targetSets`：1-99 必须
- `targetReps`：String，0-32 字符 必填（建议用户选预设："8" "8-12" "力竭" 等下拉）
- `targetWeightKg`：0-999 选填
- `restSec`：0-600 选填

**修改权限**：仅 status=DRAFT(0) 允许，其他 409 "计划不处于草稿状态，无法修改"

---

#### 状态流转接口说明

##### A. POST /training/plans/{id}/start — 开始训练

请求体：空

响应：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "planId": "2089345678901234101",
    "status": 1,
    "statusText": "进行中",
    "startedAt": "2026-08-21T15:30:00"
  }
}
```

**前端必做**：
1. 调用 start 成功后，**启动本地计时器**（MM:SS 开始累加显示）
2. 计时器 UI：大号显示 + 暂停/继续按钮（暂停只暂停显示，后端不记暂停）
3. planId 存 Pinia/localStorage，刷新页用 GET /plans/in-progress 恢复
4. 自动带入计划中的 exercises 作为初始化（targetSets/targetReps/targetWeightKg）

**错误码**：
- 404：计划不存在
- 409 PLAN_ALREADY_IN_PROGRESS：已在进行中，重复开始
- 409 PLAN_ALREADY_COMPLETED：已完成，需 copy 再开始

---

##### B. POST /training/plans/{id}/complete — 完成训练

请求体：
```json
{
  "durationSec": 3650,
  "note": "今天状态不错，最后一组差点做不动",
  "actualSets": [
    {
      "exerciseId": "2089345678901234001",
      "setNo": 1,
      "weightKg": 60.00,
      "reps": 8,
      "isCompleted": 1,
      "isWarmup": 0,
      "rpe": 7
    },
    {
      "exerciseId": "2089345678901234001",
      "setNo": 2,
      "weightKg": 65.00,
      "reps": 8,
      "isCompleted": 1,
      "isWarmup": 0,
      "rpe": 8
    }
  ]
}
```

**字段约束（严格对齐 workout_set 表）**：
- `durationSec`：必填，前端累计秒数
- `note`：选填 0-1024 字（！workout_record.note 是 VARCHAR(1024)，不是 400）
- `actualSets`：必填，至少 1 组
  - `exerciseId` 必填
  - `setNo`：1-127（TINYINT），建议前端自动递增
  - `weightKg`：DECIMAL(8,2)，选填（自重可留空）
  - `reps`：INT，选填（力竭时留空？建议留空传 null）
  - `isCompleted`：0/1，选填默认 1
  - `isWarmup`：0/1，选填默认 0
  - `rpe`：1-10，选填

**响应（成功）**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "recordId": "2089345678901234501",
    "planId": "2089345678901234101",
    "recordDate": "2026-08-21",
    "durationSec": 3650,
    "totalVolume": 1000.00,
    "totalSets": 2,
    "totalReps": 16
  }
}
```

**后端自动计算**：
```
total_volume = Σ(weightKg × reps)，其中 is_warmup=1 的组不参与容量统计（按健身惯例）
total_sets   = count(actualSets where is_completed=1)
total_reps   = Σ(reps where is_completed=1)
record_date  = completed_at 的当天日期（DATE 类型）
```

**前端交互流程**：
```
点击「结束训练」
  ├─> 确认弹窗：训练时长 60:50（<5min 时文字变红 + 警告 icon）
  │     「确定结束？短于 5 分钟的训练将不计入记录」
  │
  ├─ [取消] ── 继续训练
  │
  └─ [确定结束] ──> 前端停止计时，durationSec 计算
                   ├─ durationSec < 300：前端提示"训练时长不足5分钟，不计入记录"，不发请求
                   └─ durationSec >= 300：Loading 调 POST /complete
                                            成功 -> 显示"训练完成"，展示 totalVolume 等统计，跳转记录详情
                                            失败 PLAN_DURATION_TOO_SHORT：后端兜底拒绝 (尽管前端已判断)
```

---

##### C. POST /training/plans/{id}/cancel — 放弃训练

请求体：空

响应：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "planId": "...",
    "status": 3,
    "statusText": "已取消"
  }
}
```

CANCELLED(3) 之后可以再次 start（无需 copy），但不生成 workout_record。

---

##### D. POST /training/plans/{id}/copy — 复制计划

请求体：空（自动命名 "推日A 副本"）

响应：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "newPlanId": "...",
    "name": "推日A 副本",
    "planType": 1,
    "planTypeLabel": "力量",
    "status": 0,
    "statusText": "草稿",
    "exerciseCount": 5
  }
}
```

---

##### E. GET /training/plans/in-progress — 恢复当前训练

有进行中：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "hasActivePlan": true,
    "planId": "2089345678901234101",
    "name": "推日A",
    "startedAt": "2026-08-21T15:30:00",
    "elapsedSec": 1500,
    "plan": { ... 完整 PlanDetailVO，含 exercises[] ... }
  }
}
```

无进行中：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "hasActivePlan": false
  }
}
```

**前端使用**：页面加载调用此接口，有活动则进入训练中页面，计时器从 `elapsedSec` 秒开始继续显示，plan.exercises 作为初始化动作列表。

---

#### 前端页面建议（训练中页面）

```
┌───────────────────────────────────────────────────────────────┐
│ ← 返回   推日A · 力量训练                         [暂停] [结束]│
├───────────────────────────────────────────────────────────────┤
│                     ⏱  01:02:30                               │
│                  大号计时器 + 暂停/继续按钮                     │
├───────────────────────────────────────────────────────────────┤
│ ▼ 杠铃卧推（建议 4组 × 8-12 建议 60kg）             [+加一组] │
│   ┌──┬────────┬───────┬──────┬────┬────┬───────┐              │
│   │#1│ 60.0kg │ 8次   │ RPE7 │ ✓  │ 否 │ [编]  │              │
│   │#2│ 65.0kg │ 8次   │ RPE8 │ ✓  │ 否 │ [编]  │              │
│   │#3│ ______ │ ____  │ ___  │ □  │ 否 │+下一组│              │
│   └──┴────────┴───────┴──────┴────┴────┴───────┘              │
├───────────────────────────────────────────────────────────────┤
│ ▼ 哑铃飞鸟（建议 3×12 建议 20kg）                    [+加一组] │
│   ...                                                         │
├───────────────────────────────────────────────────────────────┤
│ [+ 添加自由动作]                                               │
├───────────────────────────────────────────────────────────────┤
│ 实时汇总：容量 1000kg · 组数 12 · 次数 96                      │
│ （isWarmup=1 的组不参与容量统计显示，仅计入组数）              │
└───────────────────────────────────────────────────────────────┘
```

前端注意：
- 每组「isWarmup」复选框，勾上后该组颜色变浅
- 建议组次/次数/重量（来自 plan_exercise）作为输入框占位符展示

---

### 4.3 训练记录查询（WorkoutRecord）— 无提交接口

接口：
- `GET /api/v1/training/records?page=1&size=10&startDate=&endDate=`
- `GET /api/v1/training/records/{id}`

**⚠️ 已删除**：`POST /training/records`（记录只在 plan complete 自动生成）

记录详情响应中的 set：

```json
{
  "sets": [
    {
      "id": "2089345678901234601",
      "exerciseId": "2089345678901234001",
      "exerciseName": "杠铃卧推",
      "setNo": 1,
      "weightKg": 60.00,
      "reps": 8,
      "isCompleted": true,
      "isWarmup": false,
      "rpe": 7
    }
  ]
}
```

---

## 五、后端实现要点

### 5.1 实体类（严格对齐 §二.3）

| 实体 | 表 | 关键字段类型 |
|---|---|---|
| Exercise | exercise | category=Integer, difficulty=Integer, muscle_group=String(V3), is_system=Integer, user_id=Long |
| WorkoutPlan | workout_plan | plan_type=Integer, status=Integer(V3), started_at=LocalDateTime(V3), completed_at=LocalDateTime(V3), actual_duration_sec=Integer(V3), estimated_min=Integer |
| WorkoutPlanExercise | workout_plan_exercise | target_reps=String(!), rest_sec=Integer(!), target_weight_kg=BigDecimal(V3) |
| WorkoutRecord | workout_record | record_date=LocalDate(!DATE), total_volume=BigDecimal(12,2) |
| WorkoutSet | workout_set | set_no=Integer(TINYINT), weight_kg=BigDecimal(8,2), reps=Integer, is_completed=Integer, is_warmup=Integer, rpe=Integer(TINYINT) |

### 5.2 TrainingErrorCode（16 枚举）

| 枚举值 | code | message |
|---|---|---|
| EXERCISE_NOT_FOUND | 404 | 动作不存在 |
| EXERCISE_SYSTEM_CANNOT_DELETE | 409 | 系统预置动作不可删除 |
| EXERCISE_NOT_YOURS | 403 | 不是您的自定义动作，无法编辑/删除 |
| EXERCISE_NAME_DUPLICATED | 409 | 动作名称重复 |
| EXERCISE_IN_USE | 409 | 动作被计划或记录引用，不可删除 |
| PLAN_NOT_FOUND | 404 | 计划不存在 |
| PLAN_NAME_DUPLICATED | 409 | 计划名称重复 |
| PLAN_EXERCISE_EMPTY | 400 | 计划至少包含1个动作 |
| PLAN_NOT_DRAFT | 409 | 计划不处于草稿状态，无法修改 |
| PLAN_NOT_IN_PROGRESS | 409 | 计划不处于进行中状态 |
| PLAN_ALREADY_IN_PROGRESS | 409 | 计划已处于进行中，无法重复开始 |
| PLAN_ALREADY_COMPLETED | 409 | 计划已完成，请先复制再开始 |
| PLAN_DURATION_TOO_SHORT | 400 | 训练时长不足5分钟，不计入记录 |
| RECORD_NOT_FOUND | 404 | 训练记录不存在 |
| RECORD_SET_EMPTY | 400 | 训练记录至少包含1组 |
| SET_EXERCISE_NOT_FOUND | 400 | 训练明细中引用的动作不存在 |

---

## 六、任务清单（40 项，用户逐个确认执行）

### P1：实体 + Mapper + 错误枚举（11 项）

| # | 交付物 | 文件路径 | 说明 |
|---|---|---|---|
| 1.1 | Exercise | entity/Exercise.java | category=Integer, muscle_group=String(V3), user_id=Long；MyBatis-Plus 雪花/自动填充/逻辑删除 |
| 1.2 | WorkoutPlan | entity/WorkoutPlan.java | status/started/completed/actual_duration(V3), plan_type=Integer, estimated_min=Integer |
| 1.3 | WorkoutPlanExercise | entity/WorkoutPlanExercise.java | 表名 workout_plan_exercise（不是 Item），target_reps=String，rest_sec=Integer，target_weight_kg=BigDecimal(V3) |
| 1.4 | WorkoutRecord | entity/WorkoutRecord.java | record_date=LocalDate，total_volume=BigDecimal(12,2) |
| 1.5 | WorkoutSet | entity/WorkoutSet.java | set_no=Integer，is_completed/is_warmup=Integer，rpe=Integer，无 rest_sec |
| 1.6-10 | 5 个 Mapper | mapper/ExerciseMapper 等 5 个 | BaseMapper<T> |
| 1.11 | TrainingErrorCode | training/enums/TrainingErrorCode.java | 16 枚举 §5.2 |

### P2：Exercise CRUD（6 项）

| # | 交付物 | 路径 | 说明 |
|---|---|---|---|
| 2.1 | ExerciseCreateReq | training/dto/req/ExerciseCreateReq.java | category(1-8) + difficulty(1-3) 数字校验 |
| 2.2 | ExerciseUpdateReq | training/dto/req/ExerciseUpdateReq.java | 同 Create，全部可选（部分更新） |
| 2.3 | ExerciseVO | training/dto/vo/ExerciseVO.java | @Builder，含 categoryLabel/difficultyLabel/isMine 后端计算 |
| 2.4 | ExerciseService | training/service/ExerciseService.java | page/detail/create/update/delete |
| 2.5 | ExerciseServiceImpl | training/service/impl/ExerciseServiceImpl.java | 系统动作不可删/不是你的不能改/防引用删除/名称去重 |
| 2.6 | ExerciseController | training/controller/ExerciseController.java | 5 端点 + @RequestLog |

### P3：WorkoutPlan + 状态流转（14 项）

| # | 交付物 | 路径 | 说明 |
|---|---|---|---|
| 3.1 | PlanExerciseReq | training/dto/req/PlanExerciseReq.java | exerciseId/sortOrder/targetSets/targetReps(String)/targetWeightKg/restSec |
| 3.2 | PlanCreateReq | training/dto/req/PlanCreateReq.java | name/planType(1-3)/description/estimatedMin/exercises[]至少1 |
| 3.3 | PlanUpdateReq | training/dto/req/PlanUpdateReq.java | 同 Create，全可选 |
| 3.4 | PlanCompleteReq | training/dto/req/PlanCompleteReq.java | durationSec/note/actualSets[]（ActualSetInput 内部类：exerciseId/setNo/weightKg/reps/isCompleted/isWarmup/rpe） |
| 3.5 | PlanExerciseVO | training/dto/vo/PlanExerciseVO.java | exerciseId+exerciseName+sortOrder+targetSets+targetReps(String)+targetWeightKg+restSec |
| 3.6 | PlanListVO | training/dto/vo/PlanListVO.java | planTypeLabel/statusText/exerciseCount/started/completed/actualDuration |
| 3.7 | PlanDetailVO | training/dto/vo/PlanDetailVO.java | 含 exercises[] |
| 3.8 | PlanStartResp / PlanCopyResp / InProgressVO | training/dto/vo/*.java | 3 个响应 VO，见 §四 |
| 3.9 | PlanCompleteResp | training/dto/vo/PlanCompleteResp.java | recordId/totalVolume/totalSets/totalReps |
| 3.10 | WorkoutPlanService | training/service/WorkoutPlanService.java | page/detail/create/update/delete + start/complete/cancel/copy/inProgress |
| 3.11 | WorkoutPlanServiceImpl | training/service/impl/WorkoutPlanServiceImpl.java | 核心：@Transactional complete（3 表原子：plan更新+record插入+sets插入）、状态机守卫、copy深拷贝、容量计算(isWarmup=1跳过) |
| 3.12 | WorkoutPlanController | training/controller/WorkoutPlanController.java | 10 端点 + @RequestLog |
| 3.13 | SecurityConfig | security/SecurityConfig.java | 白名单无需加，training 所有接口均需登录 |
| 3.14 | （如有必要）常量类 | common/constants/TrainingConstants.java | 状态/类别/难度枚举值做常量 |

### P4：WorkoutRecord 查询（6 项）

| # | 交付物 | 路径 | 说明 |
|---|---|---|---|
| 4.1 | RecordListVO | training/dto/vo/RecordListVO.java | planName 联查 |
| 4.2 | RecordSetVO | training/dto/vo/RecordSetVO.java | exerciseName + setNo + weightKg + reps + isCompleted + isWarmup + rpe |
| 4.3 | RecordDetailVO | training/dto/vo/RecordDetailVO.java | 含 sets[] + 统计 |
| 4.4 | WorkoutRecordService | training/service/WorkoutRecordService.java | page / detail |
| 4.5 | WorkoutRecordServiceImpl | training/service/impl/WorkoutRecordServiceImpl.java | 自定义 SQL 联查 exercise.name + plan.name |
| 4.6 | WorkoutRecordController | training/controller/WorkoutRecordController.java | 2 端点 + @RequestLog |

### P5：文档同步 + 验证（3 项）

| # | 交付物 | 说明 |
|---|---|---|
| 5.1 | 接口文档.md | 删除旧 POST /records，新增状态流转 5 接口；字段严格对齐 §二 |
| 5.2 | 接口文档_apifox.json | 同步全部 schema 和 path 定义 |
| 5.3 | V3 SQL 验证 | 手动执行 V3 后检查 10 个预置动作的 category/muscle_group 正确 |

---

## 七、设计模式学习要点（执行中补充）

（在每个 P 完成后追加实际代码中的设计应用说明）
