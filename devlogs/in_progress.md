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

### 1.2 关键业务修订（2026-08-21 讨论确认）

**核心变更**：训练记录不再手动独立提交，必须来源于训练计划的完整执行过程。

```
原方案：用户手动 POST /training/records 提交记录（planId 可选）
新方案：选择计划 → start → 前端本地计时器（实时显示）→ complete（校验≥5min）→ 系统自动生成 record
```

**关键规则**：
1. 必须选择计划才能开始训练，记录只在完成训练时系统自动生成
2. 前端本地计时器（可暂停/继续），完成时二次确认弹窗
3. 训练时长 < 5 分钟，拒绝生成记录（用户主观确认结束训练）
4. 已完成的计划可再次开始：通过 copy 接口复制新计划后 start
5. 组明细可超出 plan_item 模板（plan 只是参考）

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
- **路径**：`fitness-backend/src/main/resources/sql/migration/V3__workout_plan_extend_and_exercise_seed.sql`
- **执行前需先执行 V1(schema.sql) + V2**

### 2.2 workout_plan 新增字段

| 字段 | 类型 | 说明 |
|---|---|---|
| `started_at` | DATETIME NULL | 开始训练时间戳，start 接口写入 |
| `completed_at` | DATETIME NULL | 完成训练时间戳，complete 接口写入 |
| `actual_duration_sec` | INT NULL | 实际训练时长（秒），前端提交 |

### 2.3 status 枚举扩展（4 态）

| 值 | 枚举 | 说明 |
|---|---|---|
| 0 | DRAFT | 草稿，可编辑/可开始 |
| 1 | IN_PROGRESS | 训练中（前端计时器运行中） |
| 2 | COMPLETED | 已完成，已生成 record，**不可再次开始**（需 copy） |
| 3 | CANCELLED | 已取消，可再次 start（无需 copy） |

### 2.4 预置系统级动作（exercise.is_system=1，共 10 个）
1. 杠铃卧推 chest 2
2. 哑铃飞鸟 chest 1
3. 杠铃深蹲 leg 3
4. 腿举 leg 1
5. 引体向上 back 3
6. 坐姿划船 back 1
7. 杠铃推举 shoulder 2
8. 哑铃侧平举 shoulder 1
9. 杠铃弯举 arm 1
10. 平板支撑 core 1

---

## 三、训练模块任务拆分（修订版）

### 3.1 涉及数据表

| 表 | 关键字段 | 说明 |
|---|---|---|
| `exercise` | id, name, category, difficulty, muscle_group, equipment, description, image_url, is_system | 动作库 |
| `workout_plan` | id, user_id, name, description, status, started_at, completed_at, actual_duration_sec | 训练计划（V3 扩字段） |
| `workout_plan_item` | id, plan_id, exercise_id, sort_order, target_sets, target_reps, rest_seconds | 计划-动作关联（模板） |
| `workout_record` | id, user_id, plan_id, record_date, duration_sec, total_volume, total_sets, total_reps, note | 训练记录（仅 complete 自动生成） |
| `workout_set` | id, record_id, exercise_id, set_no, weight_kg, reps, rpe, rest_seconds | 训练组明细（实际完成数据） |

### 3.2 任务阶段（修订）

| 阶段 | 任务 | 交付物数 |
|---|---|---|
| **P1** | 实体 + Mapper + 错误枚举 | 5 实体 + 5 Mapper + 1 ErrorCode |
| **P2** | Exercise 动作库 CRUD（5 接口） | DTO + Service + Controller |
| **P3** | WorkoutPlan 训练计划 CRUD（7 接口） | DTO + Service + Controller + plan_item 全量替换 + 4 个状态流转接口（start/complete/cancel/copy） |
| **P4** | WorkoutRecord 训练记录查询（2 接口） | 列表 + 详情（删除 POST /training/records，改为 plan complete 自动生成） |
| **P5** | 接口文档同步 + 预置动作库验证 | 接口文档.md / .json 更新 |

### 3.3 计划状态流转

```
DRAFT(0) ──start()──> IN_PROGRESS(1) ──complete()──> COMPLETED(2)
   │                      │
   │                      └──cancel()──> CANCELLED(3)
   │                                      │
   └──────────── <── start() ←────────────┘  (取消后可再次开始，无需 copy)

COMPLETED(2) ──copy()──> 新 plan (DRAFT) ──start()──> ...  (已完成必须复制)
```

---

## 四、前端实现说明（供前端会话参考，修订版）

### 4.1 动作库管理（Exercise）— 不变

#### 接口清单

| 方法 | 路径 | 鉴权 |
|---|---|---|
| GET | `/api/v1/training/exercises` | 需登录 |
| GET | `/api/v1/training/exercises/{id}` | 需登录 |
| POST | `/api/v1/training/exercises` | 需登录 |
| PUT | `/api/v1/training/exercises/{id}` | 需登录 |
| DELETE | `/api/v1/training/exercises/{id}` | 需登录 |

#### 列表查询参数

```
GET /api/v1/training/exercises?page=1&size=10&name=卧推&category=chest&difficulty=2
```

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| page | int | 否 | 默认 1 |
| size | int | 否 | 默认 10 |
| name | string | 否 | 模糊搜索 |
| category | string | 否 | chest/back/leg/shoulder/arm/core/cardio |
| difficulty | int | 否 | 1=入门 2=进阶 3=达人 |

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
        "category": "chest",
        "difficulty": 2,
        "muscleGroup": "胸大肌、三头肌",
        "equipment": "barbell",
        "description": "平躺在卧推凳上，双手握杠铃下放至胸部再推起",
        "imageUrl": null,
        "isSystem": true
      }
    ],
    "total": 10,
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
  "description": "...",
  "imageUrl": "https://..."
}
```

**字段约束**：
- `name`：1-50 必填
- `category`：枚举 必填
- `difficulty`：1/2/3 必填
- `isSystem`：**后端自动设置**，前端不传（预置=1，用户自建=0）

**删除规则**：
- `isSystem=1` 返回 409 "系统预置动作不可删除"
- `isSystem=0` 逻辑删除

**前端建议**：
- 表格 + 分页 + 名称/分类/难度筛选
- 新增/编辑 Dialog
- 系统动作删除按钮置灰

---

### 4.2 训练计划管理（WorkoutPlan）— **修订核心**

#### 接口清单

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/v1/training/plans` | 当前用户的计划列表（分页） |
| GET | `/api/v1/training/plans/{id}` | 计划详情（含关联动作列表） |
| POST | `/api/v1/training/plans` | 新建计划（同时提交关联动作） |
| PUT | `/api/v1/training/plans/{id}` | 修改计划（仅 status=DRAFT 允许，全量替换 items） |
| DELETE | `/api/v1/training/plans/{id}` | 删除计划 |
| **POST** | **`/api/v1/training/plans/{id}/start`** | **开始训练（status→1，写 started_at，启动前端计时器）** |
| **POST** | **`/api/v1/training/plans/{id}/complete`** | **完成训练（校验≥5min，生成 workout_record+sets）** |
| **POST** | **`/api/v1/training/plans/{id}/cancel`** | **放弃训练（status→3，不生成记录）** |
| **POST** | **`/api/v1/training/plans/{id}/copy`** | **复制计划（含 items，新 status=0）** |
| **GET** | **`/api/v1/training/plans/in-progress`** | **查询当前进行中的训练（用于页面刷新恢复计时）** |

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
        "description": "胸+三头",
        "status": 0,
        "statusText": "草稿",
        "itemCount": 5,
        "startedAt": null,
        "completedAt": null,
        "actualDurationSec": null,
        "createdAt": "2026-08-19T10:30:00"
      }
    ],
    "total": 3,
    "page": 1,
    "size": 10
  }
}
```

**status 含义（前端必须理解）**：
- 0 草稿 / 1 进行中 / 2 已完成 / 3 已取消
- `statusText`：后端返回中文枚举描述，前端直接展示

#### 计划详情响应（含关联动作）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "2089345678901234101",
    "name": "推日A",
    "description": "胸+三头",
    "status": 0,
    "statusText": "草稿",
    "items": [
      {
        "id": "2089345678901234201",
        "exerciseId": "2089345678901234001",
        "exerciseName": "杠铃卧推",
        "sortOrder": 1,
        "targetSets": 4,
        "targetReps": 8,
        "restSeconds": 90
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
      "exerciseId": "2089345678901234001",
      "sortOrder": 1,
      "targetSets": 4,
      "targetReps": 8,
      "restSeconds": 90
    }
  ]
}
```

**字段约束**：
- `name`：1-50 必填
- `items`：至少 1 个，exerciseId 必须存在
- `sortOrder`：正整数，拖拽排序后传给后端
- `targetSets`/`targetReps`：1-99 必填
- `restSeconds`：0-600 选填

**修改权限**：仅 status=DRAFT(0) 允许修改，其余状态返回 409 "计划不处于草稿状态，无法修改"

#### ===== 状态流转接口说明 =====

---

##### **A. POST /training/plans/{id}/start — 开始训练**

**请求体**：空

**响应**：
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
1. 调用 start 成功后，**启动本地计时器**，从 `00:00` 开始累加显示
2. 计时器 UI：显示当前时长（MM:SS）+ 暂停/继续按钮（暂停只暂停显示，后端不记录暂停）
3. 将 planId 存入本地存储（localStorage/Pinia），刷新页面可通过 `GET /plans/in-progress` 恢复

**错误码**：
- 404：计划不存在
- 409：已 IN_PROGRESS（重复开始）或已 COMPLETED（需 copy）

---

##### **B. POST /training/plans/{id}/complete — 完成训练**

**请求体**：
```json
{
  "durationSec": 3650,
  "note": "今天状态不错，最后一组差点做不动",
  "actualSets": [
    {
      "exerciseId": "2089345678901234001",
      "setNo": 1,
      "weightKg": 60.0,
      "reps": 8,
      "rpe": 7,
      "restSeconds": 90
    },
    {
      "exerciseId": "2089345678901234001",
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
- `durationSec`：必填，前端累计的实际训练秒数
- `actualSets`：必填，至少 1 组
  - `exerciseId` 必填，`setNo` 正整数，`weightKg` 0-999 必填，`reps` 1-999 必填，`rpe` 1-10 选填，`restSeconds` 选填
- `note`：选填，0-400 字

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
    "totalVolume": 1000.0,
    "totalSets": 2,
    "totalReps": 16
  }
}
```

**后端自动计算字段（前端无需传）**：
```
total_volume = Σ(weightKg × reps)
total_sets   = count(actualSets)
total_reps   = Σ(reps)
record_date  = completed_at 的日期
```

**前端交互流程**：
```
点击"结束训练"按钮
    │
    ├─> 弹窗：确认结束训练吗？
    │       训练时长 60:50（<5min 显示红色警告）
    │
    ├─ [取消] ── 关闭弹窗，继续训练
    │
    └─ [确定] ── 停止本地计时器，累计 durationSec
                  ├─ durationSec < 300：前端提示"训练时长不足5分钟，不计入记录"，不调用 complete
                  └─ durationSec >= 300：调用 POST /complete，显示 Loading，成功后跳记录详情
```

**错误码**：
- 400："训练时长不足5分钟，不计入记录"（后端兜底校验，尽管前端已判断）
- 400："训练记录至少包含1组"
- 404：计划不存在
- 409：计划不在 IN_PROGRESS 状态

---

##### **C. POST /training/plans/{id}/cancel — 放弃训练**

**请求体**：空

**响应**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "planId": "2089345678901234101",
    "status": 3,
    "statusText": "已取消"
  }
}
```

**交互**：放弃后**不生成 workout_record**，plan 进入 CANCELLED(3)，可再次 start（无需 copy）

---

##### **D. POST /training/plans/{id}/copy — 复制计划**

**请求体**：空（新计划 name 自动加后缀 "推日A 副本"）

**响应**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "newPlanId": "2089345678901234199",
    "name": "推日A 副本",
    "status": 0,
    "statusText": "草稿",
    "itemCount": 5
  }
}
```

**使用场景**：COMPLETED(2) 的计划再次训练时使用；也可在任意状态下复制一份调整

---

##### **E. GET /training/plans/in-progress — 恢复当前训练**

**请求体**：空

**响应（有进行中）**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "hasActivePlan": true,
    "planId": "2089345678901234101",
    "name": "推日A",
    "startedAt": "2026-08-21T15:30:00",
    "elapsedSec": 1500
  }
}
```

**响应（无进行中）**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "hasActivePlan": false
  }
}
```

**前端使用**：页面加载时调用此接口：
- 有 `hasActivePlan=true`：自动打开训练中页面，计时器从 `elapsedSec` 秒开始继续显示
- `elapsedSec`：后端计算的 now - started_at 秒数，作为计时器初始值

---

#### 前端页面建议（训练中页面）

```
┌───────────────────────────────────────────────────────────┐
│  ← 返回  推日A                          [暂停] [结束训练] │
├───────────────────────────────────────────────────────────┤
│                   ⏱  01:02:30                            │
│               （大号计时器，暂停/继续按钮）                │
├───────────────────────────────────────────────────────────┤
│  ▼ 杠铃卧推（目标 4×8）                [+ 加一组]          │
│    ┌──┬──────┬─────┬─────┬─────┐                          │
│    │#1│ 60kg │ 8次 │ RPE7│✓完成│                          │
│    │#2│ 65kg │ 8次 │ RPE8│✓完成│                          │
│    │#3│_____ │____ │_____│+下一组│                        │
│    └──┴──────┴─────┴─────┴─────┘                          │
├───────────────────────────────────────────────────────────┤
│  ▼ 哑铃飞鸟（目标 3×12）               [+ 加一组]          │
│    ...                                                    │
├───────────────────────────────────────────────────────────┤
│  [+ 添加自由动作]                                          │
├───────────────────────────────────────────────────────────┤
│  实时汇总：容量 1000kg · 组数 12 · 次数 96                 │
└───────────────────────────────────────────────────────────┘
```

---

### 4.3 训练记录管理（WorkoutRecord）— 只有查询，无提交接口

#### 接口清单

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/v1/training/records` | 记录列表（分页 + 日期筛选） |
| GET | `/api/v1/training/records/{id}` | 记录详情（含每组明细） |

**⚠️ 已删除**：`POST /api/v1/training/records`（训练记录只在 plan complete 时系统自动生成）

#### 列表查询参数

```
GET /api/v1/training/records?page=1&size=10&startDate=2026-08-01&endDate=2026-08-21
```

#### 列表响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": "2089345678901234501",
        "planId": "2089345678901234101",
        "planName": "推日A",
        "recordDate": "2026-08-21",
        "durationSec": 3650,
        "totalVolume": 1000.0,
        "totalSets": 2,
        "totalReps": 16,
        "note": "今天状态不错"
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  }
}
```

#### 记录详情响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "2089345678901234501",
    "planId": "2089345678901234101",
    "planName": "推日A",
    "recordDate": "2026-08-21",
    "durationSec": 3650,
    "totalVolume": 1000.0,
    "totalSets": 2,
    "totalReps": 16,
    "note": "今天状态不错",
    "sets": [
      {
        "id": "2089345678901234601",
        "exerciseId": "2089345678901234001",
        "exerciseName": "杠铃卧推",
        "setNo": 1,
        "weightKg": 60.0,
        "reps": 8,
        "rpe": 7,
        "restSeconds": 90
      }
    ]
  }
}
```

**前端页面建议**：
- 列表：按 recordDate 倒序，支持日期范围筛选，显示容量/组数/次数摘要
- 详情：Drawer 展示，按 exerciseId 分组显示每组明细，大号展示 totalVolume 等统计

---

## 五、后端实现要点（供后端开发参考）

### 5.1 实体类清单

| 实体 | 表 | 位置 |
|---|---|---|
| Exercise | exercise | entity/（公共） |
| WorkoutPlan | workout_plan | entity/（公共，需新增 V3 字段） |
| WorkoutPlanItem | workout_plan_item | entity/（公共） |
| WorkoutRecord | workout_record | entity/（公共） |
| WorkoutSet | workout_set | entity/（公共） |

### 5.2 DTO 放置

| DTO | 位置 |
|---|---|
| ExerciseReqVO / ExerciseListVO | training/dto/req/ + training/dto/vo/ |
| PlanCreateReq / PlanUpdateReq / PlanListVO / PlanDetailVO / PlanStartResp / PlanCompleteReq / PlanCompleteResp / PlanCopyResp / InProgressVO | training/dto/ |
| WorkoutSetInput（complete.actualSets 内元素） | training/dto/ |
| RecordListVO / RecordDetailVO / RecordSetVO | training/dto/ |

### 5.3 设计模式预告

1. **批量操作**：plan_item 删除/插入用 `removeByIds` + `saveBatch`（MyBatis-Plus）
2. **事务管理**：`@Transactional` 用于 complete（plan 更新 + record 插入 + sets 插入 原子性）
3. **计算逻辑抽取**：容量计算 `calcTotalVolume(List<WorkoutSet>)` 私有方法
4. **状态机守卫**：start/complete/cancel 接口先校验当前 status 是否允许流转，拒绝非法跳转
5. **防并发**：使用 MyBatis-Plus 乐观锁或"更新时带上 status 条件"（`update ... where id=? and status=IN_PROGRESS`）避免并发 start/complete

### 5.4 TrainingErrorCode 完整清单

| 枚举值 | code | message |
|---|---|---|
| EXERCISE_NOT_FOUND | 404 | 动作不存在 |
| EXERCISE_SYSTEM_CANNOT_DELETE | 409 | 系统预置动作不可删除 |
| EXERCISE_NAME_DUPLICATED | 409 | 动作名称重复 |
| EXERCISE_IN_USE | 409 | 动作被计划/记录引用，不可删除 |
| PLAN_NOT_FOUND | 404 | 计划不存在 |
| PLAN_NAME_DUPLICATED | 409 | 计划名称重复 |
| PLAN_ITEM_EMPTY | 400 | 计划至少包含1个动作 |
| PLAN_NOT_DRAFT | 409 | 计划不处于草稿状态，无法修改 |
| PLAN_NOT_IN_PROGRESS | 409 | 计划不处于进行中状态 |
| PLAN_ALREADY_IN_PROGRESS | 409 | 计划已处于进行中，无法重复开始 |
| PLAN_ALREADY_COMPLETED | 409 | 计划已完成，请先复制再开始 |
| PLAN_DURATION_TOO_SHORT | 400 | 训练时长不足5分钟，不计入记录 |
| RECORD_NOT_FOUND | 404 | 训练记录不存在 |
| RECORD_SET_EMPTY | 400 | 训练记录至少包含1组 |
| SET_EXERCISE_NOT_FOUND | 400 | 训练明细中引用的动作不存在 |

---

## 六、任务清单（详细版，用户逐个确认执行）

### P1：实体 + Mapper + 错误枚举

| # | 交付物 | 文件路径 | 说明 |
|---|---|---|---|
| 1.1 | Exercise 实体 | `entity/Exercise.java` | 映射 exercise 表，含所有字段 + MyBatis-Plus 注解（雪花ID/自动填充/逻辑删除） |
| 1.2 | WorkoutPlan 实体 | `entity/WorkoutPlan.java` | 映射 workout_plan，新增 V3 字段 started_at/completed_at/actual_duration_sec |
| 1.3 | WorkoutPlanItem 实体 | `entity/WorkoutPlanItem.java` | 映射 workout_plan_item |
| 1.4 | WorkoutRecord 实体 | `entity/WorkoutRecord.java` | 映射 workout_record |
| 1.5 | WorkoutSet 实体 | `entity/WorkoutSet.java` | 映射 workout_set |
| 1.6 | ExerciseMapper | `mapper/ExerciseMapper.java` | extends BaseMapper<Exercise> |
| 1.7 | WorkoutPlanMapper | `mapper/WorkoutPlanMapper.java` | extends BaseMapper<WorkoutPlan> |
| 1.8 | WorkoutPlanItemMapper | `mapper/WorkoutPlanItemMapper.java` | extends BaseMapper<WorkoutPlanItem> |
| 1.9 | WorkoutRecordMapper | `mapper/WorkoutRecordMapper.java` | extends BaseMapper<WorkoutRecord> |
| 1.10 | WorkoutSetMapper | `mapper/WorkoutSetMapper.java` | extends BaseMapper<WorkoutSet> |
| 1.11 | TrainingErrorCode | `training/enums/TrainingErrorCode.java` | 16 个枚举值（见 §5.4） |

### P2：Exercise 动作库 CRUD（5 接口）

| # | 交付物 | 文件路径 | 说明 |
|---|---|---|---|
| 2.1 | ExerciseCreateReq | `training/dto/req/ExerciseCreateReq.java` | @Data + 校验（name/category/difficulty 必填） |
| 2.2 | ExerciseUpdateReq | `training/dto/req/ExerciseUpdateReq.java` | 同 Create，字段可选（允许部分更新） |
| 2.3 | ExerciseVO | `training/dto/vo/ExerciseVO.java` | 列表/详情响应，@Builder |
| 2.4 | ExerciseService 接口 | `training/service/ExerciseService.java` | page / detail / create / update / delete |
| 2.5 | ExerciseServiceImpl | `training/service/impl/ExerciseServiceImpl.java` | 业务逻辑：系统动作不可删、名称去重、防引用删除 |
| 2.6 | ExerciseController | `training/controller/ExerciseController.java` | 5 个端点，@RequestLog |

### P3：WorkoutPlan 训练计划 + 状态流转（10 接口）

| # | 交付物 | 文件路径 | 说明 |
|---|---|---|---|
| 3.1 | PlanItemReq | `training/dto/req/PlanItemReq.java` | plan.item 子对象（exerciseId/sortOrder/targetSets/targetReps/restSeconds） |
| 3.2 | PlanCreateReq | `training/dto/req/PlanCreateReq.java` | name/description/items[至少1] |
| 3.3 | PlanUpdateReq | `training/dto/req/PlanUpdateReq.java` | 同 Create，字段可选 |
| 3.4 | PlanCompleteReq | `training/dto/req/PlanCompleteReq.java` | durationSec/note/actualSets[]（含 WorkoutSetInput 内部类） |
| 3.5 | PlanItemVO | `training/dto/vo/PlanItemVO.java` | 详情响应 item：exerciseId+exerciseName+排序+目标组次+休息 |
| 3.6 | PlanListVO | `training/dto/vo/PlanListVO.java` | 列表卡：status+statusText+itemCount+startedAt+completedAt |
| 3.7 | PlanDetailVO | `training/dto/vo/PlanDetailVO.java` | 详情：含 items[] |
| 3.8 | PlanStartResp | `training/dto/vo/PlanStartResp.java` | start 返回：planId/status/startedAt |
| 3.9 | PlanCompleteResp | `training/dto/vo/PlanCompleteResp.java` | complete 返回：recordId/totalVolume/totalSets/totalReps |
| 3.10 | PlanCopyResp | `training/dto/vo/PlanCopyResp.java` | copy 返回：newPlanId/name/status/itemCount |
| 3.11 | InProgressVO | `training/dto/vo/InProgressVO.java` | hasActivePlan/planId/name/startedAt/elapsedSec |
| 3.12 | WorkoutPlanService 接口 | `training/service/WorkoutPlanService.java` | page/detail/create/update/delete + start/complete/cancel/copy/inProgress |
| 3.13 | WorkoutPlanServiceImpl | `training/service/impl/WorkoutPlanServiceImpl.java` | 核心：items 全量替换、@Transactional complete（3表操作）、状态机校验、容量计算、copy 深拷贝 |
| 3.14 | WorkoutPlanController | `training/controller/WorkoutPlanController.java` | 10 个端点，@RequestLog |

### P4：WorkoutRecord 训练记录查询（2 接口）

| # | 交付物 | 文件路径 | 说明 |
|---|---|---|---|
| 4.1 | RecordListVO | `training/dto/vo/RecordListVO.java` | 列表响应 |
| 4.2 | RecordSetVO | `training/dto/vo/RecordSetVO.java` | 明细 set：exerciseName + setNo + weightKg + reps + rpe |
| 4.3 | RecordDetailVO | `training/dto/vo/RecordDetailVO.java` | 详情：含 sets[] + 统计 |
| 4.4 | WorkoutRecordService 接口 | `training/service/WorkoutRecordService.java` | page / detail |
| 4.5 | WorkoutRecordServiceImpl | `training/service/impl/WorkoutRecordServiceImpl.java` | 自定义 SQL 联查 exercise 名称 |
| 4.6 | WorkoutRecordController | `training/controller/WorkoutRecordController.java` | 2 个端点，@RequestLog |

### P5：接口文档同步 + 验证

| # | 交付物 | 文件路径 | 说明 |
|---|---|---|---|
| 5.1 | 接口文档.md | `docs/接口文档.md` | 删除旧记录提交接口，新增计划状态流转接口 |
| 5.2 | 接口文档_apifox.json | `docs/接口文档_apifox.json` | 同步 schema 和 path 定义 |
| 5.3 | V3 SQL 验证 | - | 手动执行 SQL 确认 10 个预置动作数据正确 |

---

## 七、设计模式学习要点（执行中按 P 补充）

（本章节在每个 P 完成后追加实际代码中的设计应用说明）
