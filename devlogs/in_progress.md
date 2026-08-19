# FitPulse 后端开发日志（进行中）

> 会话主题：User 模块开发（资料/账号/密码/头像/统计）+ File 模块最小实现
> 起始时间：2026-08-19
> 状态：进行中（会话结束后由用户确认，重命名为 `yyyyMMdd_<会话概括>.md`）

---

## 一、需求与上下文

### 1.1 用户原始需求
1. 先做 User 模块（作为其他业务模块的基础依赖）
2. PC 端与移动端后端**合并为单一 user 模块**（不做 user/admin 分拆，符合"单账号个人使用"设计）
3. PC 端个人中心展示信息过少，**在现有表字段上扩展有用功能**
4. 本次范围：
   - ✅ 修改接口组（资料 / 账号 / 密码）
   - ✅ 头像上传接口（含 File 模块最小实现）
   - ✅ 训练统计 / 健康概览聚合接口
   - ⏸️ 用户目标功能（user_goal）暂时搁置
5. 数据库变更方式：**ALTER TABLE 追加字段**（不重写 schema.sql，保留已有数据）
6. 前端原型开发计划写入本日志，**后续会话单独执行**

### 1.2 全局规则提醒
- 每次制定任务先与用户讨论确认后执行
- 每次改动立即 git commit
- devlogs 临时日志会话结束后重命名归档
- Service 层接口-impl 分离
- 每个模块独立错误枚举实现 BaseExceptionInterface
- 后端 JDK 21，API 统一前缀 `/api/v1`
- 变量声明禁用 var

---

## 二、数据库扩展设计

### 2.1 现有字段盘点

**user 表**（schema.sql 已有）：
- id, username, password_hash, email, **phone**（实体未映射）, status, last_login_at, created_at, updated_at, deleted

**user_profile 表**（schema.sql 已有）：
- id, user_id, nickname, avatar_url, gender, birthday, height_cm, bio, created_at, updated_at, deleted

### 2.2 ALTER TABLE 追加字段

在 `user_profile` 表追加 4 个字段：

```sql
ALTER TABLE user_profile
    ADD COLUMN weight_kg      DECIMAL(5,1) NULL COMMENT '当前体重kg（缓存最新值）' AFTER height_cm,
    ADD COLUMN body_fat_pct   DECIMAL(4,1) NULL COMMENT '当前体脂率%（缓存最新值）' AFTER weight_kg,
    ADD COLUMN fitness_level  TINYINT      NULL COMMENT '健身等级 1=入门 2=进阶 3=达人 4=专业' AFTER body_fat_pct,
    ADD COLUMN theme          TINYINT      NULL DEFAULT 1 COMMENT '主题偏好 1=浅色 2=深色 3=跟随系统' AFTER fitness_level;
```

**设计理由**：
- `weight_kg` / `body_fat_pct`：body_metric 表有历史记录，profile 存最新值做快速展示，避免每次联表查最新一条
- `fitness_level`：增加用户画像维度，后续可用于训练推荐
- `theme`：PC 端设置项需要持久化

---

## 三、后端任务拆分（P1-P5）

### P1 - 数据库与实体层
| # | 任务 | 文件 |
|---|---|---|
| 1.1 | 编写 ALTER TABLE 追加字段 SQL | `fitness-backend/src/main/resources/sql/migration/V2__user_profile_extend.sql` |
| 1.2 | User.java 补充 phone 字段映射 | `entity/User.java` |
| 1.3 | 新建 UserProfile.java 实体（含新字段） | `entity/UserProfile.java` |

### P2 - DTO 与 Mapper 层
| # | 任务 | 文件 |
|---|---|---|
| 2.1 | 设计 UserProfileVO（响应 DTO，聚合 user + user_profile） | `user/dto/resp/UserProfileVO.java` |
| 2.2 | 设计 UpdateProfileReq（更新基本资料） | `user/dto/req/UpdateProfileReq.java` |
| 2.3 | 设计 UpdateAccountReq（更新邮箱、手机） | `user/dto/req/UpdateAccountReq.java` |
| 2.4 | 设计 ChangePasswordReq（修改密码） | `user/dto/req/ChangePasswordReq.java` |
| 2.5 | 创建 UserProfileMapper | `user/mapper/UserProfileMapper.java` |
| 2.6 | 确认 UserMapper 状态（已存在则复用） | `auth/mapper/UserMapper.java` |

### P3 - Service 与 Controller 层
| # | 任务 | 文件 |
|---|---|---|
| 3.1 | 创建 UserErrorCode 错误枚举 | `user/enums/UserErrorCode.java` |
| 3.2 | 创建 UserService 接口 | `user/service/UserService.java` |
| 3.3 | 创建 UserServiceImpl 实现 | `user/service/impl/UserServiceImpl.java` |
| 3.4 | 创建 UserController（profile / account / password 接口） | `user/controller/UserController.java` |

### P4 - File 模块最小实现
| # | 任务 | 文件 |
|---|---|---|
| 4.1 | 创建 FileResource 实体 | `entity/FileResource.java` |
| 4.2 | 创建 FileResourceMapper | `file/mapper/FileResourceMapper.java` |
| 4.3 | 创建 FileStorageService 接口 | `file/service/FileStorageService.java` |
| 4.4 | 创建 LocalFileStorageServiceImpl（本地存储实现） | `file/service/impl/LocalFileStorageServiceImpl.java` |
| 4.5 | 创建 FileController（POST /file/upload） | `file/controller/FileController.java` |
| 4.6 | 配置文件存储参数（application.yml） | `application.yml` + `application-demo.yml` |

### P5 - 头像上传与统计接口
| # | 任务 | 文件 |
|---|---|---|
| 5.1 | POST /user/avatar 接口（调用 FileStorageService） | `user/controller/UserController.java` 追加 |
| 5.2 | GET /user/stats 训练统计聚合 | `user/controller/UserController.java` 追加 |
| 5.3 | GET /user/overview 健康概览聚合 | `user/controller/UserController.java` 追加 |
| 5.4 | WorkoutRecordMapper 聚合查询方法 | `training/mapper/WorkoutRecordMapper.java` |
| 5.5 | BodyMetricMapper 聚合查询方法 | `health/mapper/BodyMetricMapper.java` |
| 5.6 | MealRecordMapper 聚合查询方法 | `health/mapper/MealRecordMapper.java` |
| 5.7 | WaterLogMapper 聚合查询方法 | `health/mapper/WaterLogMapper.java` |

---

## 四、API 设计清单

| # | 方法 | 路径 | 说明 | 认证 |
|---|---|---|---|---|
| 1 | GET | `/api/v1/user/profile` | 获取用户完整资料（user + user_profile 联查） | ✅ |
| 2 | PUT | `/api/v1/user/profile` | 更新基本资料（昵称/性别/生日/身高/体重/体脂/简介/等级） | ✅ |
| 3 | PUT | `/api/v1/user/account` | 更新账号信息（邮箱、手机） | ✅ |
| 4 | PUT | `/api/v1/user/password` | 修改密码（旧密码验证 + 新密码） | ✅ |
| 5 | POST | `/api/v1/user/avatar` | 上传头像（multipart → file 模块 → 更新 avatar_url） | ✅ |
| 6 | GET | `/api/v1/user/stats` | 训练统计概览（累计次数/容量/连续天数/最近训练） | ✅ |
| 7 | GET | `/api/v1/user/overview` | 健康概览（最新体重体脂/今日热量/今日饮水） | ✅ |
| 8 | POST | `/api/v1/file/upload` | 文件上传（通用接口，bucket 区分头像/动作图/饮食照） | ✅ |

---

## 五、前端原型开发计划（后续会话执行）

### 5.1 开发方式
- 使用 `rapid-prototype-craft` 技能快速生成高保真原型
- 静态 mock 数据，不接真实 API
- 目标项目：`fitness-web-admin`

### 5.2 页面结构（PC 端个人中心 6 Tab）

| Tab | 标题 | 展示内容 | 数据字段（mock） |
|---|---|---|---|
| 1 | 基本资料 | 头像 + 昵称 + 性别 + 生日 + 身高 + 体重 + 体脂 + 简介 + 健身等级 | nickname, avatar_url, gender, birthday, height_cm, weight_kg, body_fat_pct, bio, fitness_level |
| 2 | 账号安全 | 用户名（只读）+ 邮箱（可改）+ 手机（可改）+ 修改密码入口 | username, email, phone |
| 3 | 训练统计 | 累计训练 128 次 · 总容量 56,820kg · 连续 7 天 · 上次训练 2026-08-18 | 聚合数据 |
| 4 | 健康概览 | 体重 72.5kg · 体脂 18.2% · 今日热量 1,450kcal · 饮水 1,200ml | 聚合数据 |
| 5 | 设置 | 主题切换（浅/深/跟随系统）+ 清除缓存 | theme |
| 6 | 关于 | 版本号 v1.0.0 + 退出登录按钮 | 静态 |

### 5.3 Mock 数据样例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 1873487291038476288,
    "username": "fire_dev",
    "email": "fire_dev@qq.com",
    "phone": "138****8888",
    "status": 1,
    "lastLoginAt": "2026-08-19 09:30:15",
    "createdAt": "2026-01-15 10:00:00",
    "profile": {
      "nickname": "Fire健身达人",
      "avatarUrl": "/mock/avatar.png",
      "gender": 1,
      "birthday": "1998-05-20",
      "heightCm": 175.0,
      "weightKg": 72.5,
      "bodyFatPct": 18.2,
      "bio": "坚持健身3年，目标是80kg卧推",
      "fitnessLevel": 3,
      "theme": 1
    }
  }
}
```

### 5.4 组件清单
- `UserProfileCard.vue` - 用户资料卡片
- `AvatarUploader.vue` - 头像上传组件
- `StatsOverview.vue` - 训练统计概览
- `HealthOverview.vue` - 健康概览
- `ThemeSwitcher.vue` - 主题切换
- `ChangePasswordDialog.vue` - 修改密码弹窗

### 5.5 后续接入说明
原型完成后，将 axios baseURL 从 mock 切换到真实接口即可联调：
- `GET /user/profile` → 替换 mock 用户数据
- `PUT /user/profile` → 接入表单提交
- `POST /user/avatar` → 接入头像上传
- `GET /user/stats` / `GET /user/overview` → 接入统计数据

---

## 六、执行记录

### P1 - 数据库与实体层（2026-08-19 完成）

**P1.1 ALTER TABLE SQL**：创建 `fitness-backend/src/main/resources/sql/migration/V2__user_profile_extend.sql`，在 user_profile 表追加 4 个字段：
- `weight_kg DECIMAL(5,1)` - 当前体重（缓存最新值）
- `body_fat_pct DECIMAL(4,1)` - 当前体脂率（缓存最新值）
- `fitness_level TINYINT` - 健身等级 1=入门 2=进阶 3=达人 4=专业
- `theme TINYINT DEFAULT 1` - 主题偏好 1=浅色 2=深色 3=跟随系统

**P1.2 User.java 补全**：补充 phone 字段映射（schema.sql 中已有但实体未映射）。

**P1.3 UserProfile.java 新建**：完整实体类，映射 user_profile 表所有字段（含 V2 新增的 weightKg / bodyFatPct / fitnessLevel / theme）。

**编译验证**：`mvn compile` 通过，无 warning。

### 接口文档同步（2026-08-19 完成）

**md 文件**（接口文档.md）：
- 删除未开发模块：§三看板、§四训练、§五健康、§七AI（共 4 个模块 20+ 接口）
- 更新 §三用户模块（原§八）为新设计：7 个接口（profile GET/PUT、account PUT、password PUT、avatar POST、stats GET、overview GET）
- 保留 §四文件模块（P4 将开发）
- 版本升级 v1.0 → v1.1，添加"仅含已开发/本次计划开发"声明
- 删除分页参数约定（1.3 节，本次不涉及分页接口）

**json 文件**（接口文档_apifox.json）：
- 从 3268 行精简到 1361 行
- 删除未开发模块的 paths：/admin/dashboard/*、/training/*、/health/*、/ai/*
- 删除未使用的 schemas：TrainingOverview、HealthOverview、ExerciseVO、PlanVO、RecordVO、BodyMetricVO、FoodVO、MealVO 等 20+ 个
- 新增 user 模块 schemas：UserProfileVO、UpdateProfileReq、UpdateAccountReq、ChangePasswordReq、AvatarUploadVO、TrainingStatsVO、HealthOverviewVO
- 新增 file 模块 schemas：FileUploadVO
- 补充 RefreshReq schema（原 paths 引用但 schemas 未定义）
- tags 精简为 3 个：Auth、User、File

**git 提交**：P1 + 文档同步合并提交。

### P2 - DTO 与 Mapper 层（2026-08-19 完成）

**P2.1 UserProfileVO**：`user/dto/vo/UserProfileVO.java`，响应 DTO，聚合 user + user_profile 联查。内嵌 Profile 静态类，包含 V2 新增字段（weightKg / bodyFatPct / fitnessLevel / theme）。使用 @Builder 注解与 LoginUserVO 风格一致。

**P2.2 UpdateProfileReq**：`user/dto/req/UpdateProfileReq.java`，更新基本资料请求。所有字段可空（部分更新语义），使用 @DecimalMin/@DecimalMax 校验数值范围（身高 50-300cm，体重 20-500kg，体脂 3-60%）。

**P2.3 UpdateAccountReq**：`user/dto/req/UpdateAccountReq.java`，更新账号信息请求。邮箱 @Pattern 限定 @qq.com，手机号 @Pattern 限定 11 位数字。

**P2.4 ChangePasswordReq**：`user/dto/req/ChangePasswordReq.java`，修改密码请求。newPassword 使用与 RegisterReq 相同的 @Size + @Pattern 校验规则（8-64 位，至少含字母+数字）。

**P2.5 UserProfileMapper**：`mapper/UserProfileMapper.java`，放在 common/mapper 包下与 UserMapper 保持一致（实体共享，Mapper 共享）。

**P2.6 UserMapper 确认**：已存在于 `mapper/UserMapper.java`，直接复用。

### P3 - Service 与 Controller 层（2026-08-19 完成）

**P3.1 UserErrorCode**：`user/enums/UserErrorCode.java`，8 个枚举值：
- 404: USER_NOT_FOUND / USER_PROFILE_NOT_FOUND
- 401: OLD_PASSWORD_ERROR
- 409: EMAIL_ALREADY_USED / PHONE_ALREADY_USED
- 400: PASSWORD_CONFIRM_NOT_MATCH / NO_FIELDS_TO_UPDATE

**P3.2 UserService 接口**：`user/service/UserService.java`，4 个方法：getProfile / updateProfile / updateAccount / changePassword。

**P3.3 UserServiceImpl**：`user/service/impl/UserServiceImpl.java`，完整实现：
- getProfile: user + user_profile 联查，profile 不存在返回空对象
- updateProfile: profile 不存在则 INSERT，存在则 UPDATE，仅更新非 null 字段
- updateAccount: 邮箱/手机号变更时检查唯一性（排除当前 userId）
- changePassword: 旧密码 BCrypt 比对，通过后加密新密码更新

**P3.4 UserController**：`user/controller/UserController.java`，4 个端点：
- GET /api/v1/user/profile
- PUT /api/v1/user/profile
- PUT /api/v1/user/account
- PUT /api/v1/user/password

均标注 @RequestLog 注解，使用 CurrentUser.getUserId() 获取当前用户。

**编译验证**：`mvn compile` 通过，exit code 0。

**代码风格对齐 auth 模块**：
- @Slf4j + @Service + @RequiredArgsConstructor 三件套
- 私有常量 private static final
- 关键节点 log.info 记录（带 userId 上下文）
- 异常用 UserErrorCode 模块专属枚举
- DTO 使用 @Builder + @NoArgsConstructor + @AllArgsConstructor
- Controller 使用 @Valid + @RequestBody + Result<T>

**git 提交**：P2 + P3 合并提交。

---

## 七、管理端用户界面开发（2026-08-19）

### 7.1 范围与形态（与用户讨论确认）

- **范围**：个人中心 + 维度看板（本轮实现：训练看板 / 健康看板；个人中心暂占位，下轮 6 Tab）
- **形态**：Vue3 + Pinia + Element Plus + ECharts + Tailwind，基于现有 `fitness-web-admin` 工程延续
- **风格**：数据看板风（高信息密度、多卡片网格、维度色固化、ECharts 趋势图）
- **数据源**：开发期走前端 mock（`USE_MOCK=true`），切换真实后端只需在 `.env` 设 `VITE_USE_MOCK=false`
- **设计契约依据**：`docs/设计契约.md §4.1` 路由 + `§5` 看板维度色 + `§6.2` 看板响应字段

### 7.2 看板维度色固化（§5）

| 维度 | 强调色 | 用途 |
|---|---|---|
| A | 🟣 紫 #8E24AA | 健康：体重 / 体脂 |
| B | 🔵 蓝 #1E88E5 | 训练：容量 / 组数 / 次数 |
| C | 🟢 绿 #43A047 | 训练：趋势 / 完成率 / 连续打卡 |
| Bo | 🟠 橙 #FF6F00 | 健康：热量 / 饮水 |

### 7.3 任务拆分（d1-d11，本轮执行）

| # | 任务 | 状态 | 关键产物 |
|---|---|---|---|
| d1 | 主题 CSS 变量 + 深色主题支持 | ✅ | `tailwind.config.js`（darkMode:class + dim 色）、`src/style.scss`（浅/深 CSS 变量 + fp-card/fp-stat/fp-grid/fp-progress）、`src/stores/theme.js`（toggle/set/apply + localStorage 持久化）、`main.js` 启动应用主题、`index.html body.fp-app` |
| d2 | Mock 层 + api 层 | ✅ | `src/config/index.js`（USE_MOCK 开关）、`src/mock/dashboard.js`（TrainingOverview/HealthOverview 数据，对齐 §6.2 字段）、`src/mock/user.js`（资料+目标 mock）、`src/api/dashboard.js`、`src/api/user.js`（开关切换真/mock） |
| d3 | 通用组件 | ✅ | `src/utils/echarts.js`（按需注册 Line/Bar/Pie/Tooltip/Legend 等）、`src/components/StatCard.vue`（维度色边条 A/B/C/Bo/muted）、`src/components/ChartLine.vue`（平滑曲线 + 区域填充 + 暗色 tooltip）、`src/components/ChartBar.vue`（渐变柱 + 圆角柱顶） |
| d4 | Admin Shell + 路由整合 | ✅ | `src/layout/Layout.vue`（侧边栏 + 顶栏 + 内容区 + 主题切换按钮 + 用户下拉退出 + 响应式窄屏折叠）、`src/views/profile/Profile.vue` 占位、删除 `src/views/home/Home.vue`（登录成功占位已被看板取代）、`router/index.js` 嵌套结构：`/` → Layout，children 含 `/dashboard/{training,health}` 与 `/profile`，`/` 重定向到 `/dashboard/training` |
| d5 | 训练看板 | ✅ | `src/views/dashboard/Training.vue`：B 三卡（容量/组数/次数）+ C 两卡（完成率/连续打卡）+ 辅助两卡（次数/计划数）+ 7 天容量折线（B 维蓝）+ 7 天明细表（含平均单组容量） |
| d6 | 健康看板 | ✅ | `src/views/dashboard/Health.vue`：A 两卡（体重/体脂）+ B 两卡（热量/饮水）+ 辅助 1 卡（蛋白质）+ 独立饮水进度条卡 + 30 天体重折线（A 维紫）+ 7 天热量柱（B 维橙） |
| d11 | 浏览器验证 + devlog + git 提交 | ✅ | 浏览器子代理 PASS（见 §7.4） |
| d7 | [下轮] 个人中心 6 Tab | ⏸️ | 容器 + 基本资料/账号安全/训练统计/健康概览/设置/关于 |

### 7.4 浏览器验证结果（Vite dev @ :5174）

**验证手段**：浏览器子代理 → 在 localStorage 注入 mock token 绕过 auth 守卫 → 访问三个路由 + 主题切换 + 截图。

| 检查项 | 结果 | 证据 |
|---|---|---|
| 三页正常渲染 | ✅ | 训练看板、健康看板、个人中心均加载并展示标题与内容（截图已保存） |
| 主题切换生效 | ✅ | 点击顶栏"切换主题"按钮 → 深色模式生效：背景变深蓝灰、卡片/文字颜色反转、图表区域同步变暗 |
| 图表显示数据 | ✅ | 健康看板见 30 天体重折线 + 饮水进度条（1450/2000ml）；训练看板指标卡与明细表数值正常 |
| 控制台错误 | ⚠️ | `net::ERR_ABORTED` 资源加载中断（echarts.js / element-plus CSS / vue.js 等），属 SPA 路由切换取消 in-flight 模块请求的常见现象，不影响功能 |

### 7.5 字段对齐核查（设计契约 §6.2）

**TrainingOverview**（mock 实际返回）：
- `totalWorkoutsThisWeek / totalVolumeThisWeek / totalSetsThisWeek / totalRepsThisWeek / completionRate7d / streakDays / totalPlans / weeklyVolumeTrend[{date,volume,sets}]` —— 8 字段全对齐

**HealthOverview**（mock 实际返回）：
- `latestWeight / latestBodyFat / weightTrend30d[{date,value}] / caloriesToday / waterTodayMl / waterGoalMl / proteinTodayG / caloriesLast7d[{date,value}]` —— 8 字段全对齐

### 7.6 后续接入说明

切换真实后端：
1. 项目根目录新建 `.env` 文件，写入 `VITE_USE_MOCK=false`
2. 后端实现 `/api/v1/admin/dashboard/training` 与 `/api/v1/admin/dashboard/health` 接口（响应字段名严格对齐 §6.2）
3. 无需改前端业务代码 —— `api/dashboard.js` 与 `api/user.js` 会自动走真实 axios 请求

### 7.7 git 提交

本轮 d1-d11 完成后统一提交。

---

## 八、个人中心 6 Tab 实现（2026-08-19，d7）

### 8.1 任务拆分（d7.1-d7.5）

| # | 任务 | 状态 | 关键产物 |
|---|---|---|---|
| d7.1 | 扩展 mock + api 层 | ✅ | `mock/user.js` 补 weightKg/bodyFatPct/fitnessLevel/phone/theme 字段；新增 `getMyTrainingStats()` / `getMyHealthOverview()` / `uploadAvatar()` 三个 mock；`api/user.js` 新增对应接口，路径 `/user/stats` `/user/overview` `/user/avatar`（对齐 in_progress.md §四 计划） |
| d7.2 | 主题升级 3 态 | ✅ | `stores/theme.js` 重写：state.mode (light/dark/auto) + getters.resolved/isDark/isAuto + actions.apply/cycle/toggle/set；auto 模式监听 `matchMedia('prefers-color-scheme')` 实时刷新；`Layout.vue` 顶栏按钮改用 `cycle()` + 三图标（Sunny/Moon/Monitor）+ 动态 tooltip |
| d7.3 | Profile 容器重写 | ✅ | `views/profile/Profile.vue` 重写为 `el-tabs` 容器，6 个 tab-pane 懒加载（首次切换才挂子组件） |
| d7.4 | 6 个 Tab 子组件 | ✅ | `views/profile/tabs/` 下 6 个文件（详见 §8.2） |
| d7.5 | 验证 + devlog + git | ✅ | 浏览器子代理 PASS（详见 §8.3） |

### 8.2 6 个 Tab 子组件

| Tab | 文件 | 关键内容 |
|---|---|---|
| 基本资料 | `BasicTab.vue` | el-upload custom http-request 头像上传（mock 返回 trae-api URL）；资料表单（昵称/性别/生日/身高/健身等级/简介）；体重/体脂只读（缓存自 body_metric）；训练目标子表单（goalType/targetWeight/targetBodyFat/weeklyWorkouts/dailyCalories/dailyWaterMl/targetDate）独立保存 |
| 账号安全 | `AccountTab.vue` | username 只读（disabled + tooltip）；email/phone 可改；修改密码弹窗（el-dialog + el-form rules 前端校验：两次密码一致性、长度 6-32） |
| 训练统计 | `TrainingTab.vue` | 4 主卡（B 累计次数/总容量 + C 当前连续/最长连续）；4 辅助卡（总组数/总次数/平均单组容量/平均单次容量）；6 月容量柱图（ChartBar B 维蓝）；上次训练时间 + 距今天数 |
| 健康概览 | `HealthTab.vue` | 4 主卡（A 体重+30天变化 / A 体脂+30天变化 / B 今日热量 / B 今日饮水）；独立进度条卡（热量 Bo 橙 + 饮水 B 蓝）；4 辅助卡（蛋白质/睡眠/30天体重变化/30天体脂变化） |
| 设置 | `SettingsTab.vue` | 3 态主题选择器（卡片式：浅色/深色/跟随系统，含视觉预览块 + 当前态 ✓）；清除缓存按钮（保留 `fitpulse_token/rt/user/theme` 4 个键，其余 localStorage 全清）；实时生效信息卡 |
| 关于 | `AboutTab.vue` | 应用 Logo（品牌紫渐变 + 心形 SVG）；版本 v1.0.0 + 构建时间；11 个技术栈标签；项目说明；退出登录按钮（调 userStore.logout → 跳登录页） |

### 8.3 浏览器验证结果

| 检查项 | 结果 | 证据 |
|---|---|---|
| 基本资料 Tab 字段完整性 | ✅ | 头像 + 昵称 + 性别 + 生日 + 身高 + 体重 + 体脂率 + 健身等级 + 简介 + 训练目标分区全部渲染（截图保存） |
| 账号安全 Tab 用户名只读 + 修改密码弹窗 | ✅ | username 字段 disabled；点击"修改密码"按钮弹窗正常弹出 |
| 训练统计 Tab 4 主卡 + 柱图 | ✅ | 6 张统计卡有数值，6 月容量柱图挂载并显示曲线 |
| 健康概览 Tab 4 主卡 + 2 进度条 | ✅ | 体重/体脂/热量/饮水 4 卡数值正常，热量摄入与饮水进度条渲染 |
| 设置 Tab 3 主题卡 + 切换 | ✅ | 默认选项高亮；点击浅色/深色卡后整体 UI 颜色立即跟随变化 |
| 关于 Tab 版本号 + 退出登录按钮 | ✅ | 显示 v1.0.0 + 构建时间 + 退出登录按钮 |
| 控制台错误 | ⚠️ | `net::ERR_ABORTED` 资源中断（Vite HMR / 路由切换取消，非阻塞）；Vue Router 路由不匹配警告（首次无 token 访问触发 redirect 的过渡警告，登录后无） |

### 8.4 字段对齐核查

**profile mock 扩展字段**（对齐 in_progress.md §2.2 ALTER TABLE 计划）：
- `weightKg` / `bodyFatPct` / `fitnessLevel` / `theme` —— 全部加入

**新增接口字段**（对齐 in_progress.md §四 API 设计清单 6/7）：
- `/user/stats` → totalWorkouts / totalVolume / totalSets / totalReps / streakDays / longestStreak / lastWorkoutAt / monthlySummary[]
- `/user/overview` → latestWeight / latestBodyFat / weightChange30d / bodyFatChange30d / caloriesToday / caloriesGoal / waterTodayMl / waterGoalMl / proteinTodayG / proteinGoalG / sleepHoursLastNight

### 8.5 主题切换兼容性说明

- 旧 `toggle()` 方法保留（light ↔ dark），不破坏原有调用
- 新增 `cycle()` 方法供顶栏按钮使用（light → dark → auto → light）
- 新增 `set(mode)` 方法供 SettingsTab 直接选择
- `auto` 模式通过 `matchMedia` 监听系统主题变化，无需刷新页面即可实时切换

### 8.6 后续接入说明

切换真实后端（在 `.env` 设 `VITE_USE_MOCK=false`）后：
- 后端需实现 `/api/v1/user/stats` 与 `/api/v1/user/overview` 两个聚合接口（字段对齐 §8.4）
- 头像上传 `/api/v1/user/avatar` 接口需 multipart/form-data，返回 `{ avatarUrl, uploadedAt }`
- 主题持久化目前仅前端 localStorage；如需后端持久化，可在 user_profile 表的 `theme` 字段同步

### 8.7 git 提交

本轮 d7.1-d7.5 完成后统一提交。

---

## 九、体脂率前端估算决策（2026-08-19）

### 9.1 背景

用户填写个人资料时，身高和体重容易获取，但大部分用户不知道自己的体脂率。为提升填写体验，需要在用户填入身高/体重/性别/生日后，前端以浅色显示根据这些数据计算后的体脂率建议值。

### 9.2 决策结论：不需要后端参与

| 维度 | 理由 |
|---|---|
| 数据来源 | 身高、体重、性别、生日都在表单中已有，前端本地即可计算 |
| 计算复杂度 | 纯数学公式，JS 一行搞定，无需数据库或外部 API |
| 实时性要求 | 用户输入身高/体重后应立即看到建议值，前端计算零延迟，后端会有网络往返 |
| 持久化需求 | 计算结果只是"建议参考"显示给用户看，不需要存库（用户确认填入的体脂率才存 user_profile.body_fat_pct） |
| 多端复用 | 虽然有 PC 和移动端两个前端，但体脂率估算公式是公开标准，前端各自实现几行代码即可 |

### 9.3 前端实现建议

#### 9.3.1 估算公式（基于 BMI 法，成年人）

```javascript
/**
 * 基于 BMI 估算体脂率（仅供参考，非临床精确值）
 * @param {number} weightKg  体重 kg
 * @param {number} heightCm  身高 cm
 * @param {number} gender    性别 1=男 2=女
 * @param {string} birthday  生日 yyyy-MM-dd
 * @returns {number|null}    体脂率 %（保留1位小数，限制 3-60%）
 */
function estimateBodyFat(weightKg, heightCm, gender, birthday) {
    if (!weightKg || !heightCm || !gender || !birthday) return null
    const g = (gender === 1) ? 1 : 0  // 男=1 女=0
    const age = calcAge(birthday)
    const bmi = weightKg / Math.pow(heightCm / 100, 2)
    const bodyFat = (1.2 * bmi) + (0.23 * age) - (10.8 * g) - 5.4
    return Math.max(3, Math.min(60, Number(bodyFat.toFixed(1))))
}

// 辅助：根据生日算年龄
function calcAge(birthday) {
    const birth = new Date(birthday)
    const now = new Date()
    let age = now.getFullYear() - birth.getFullYear()
    const m = now.getMonth() - birth.getMonth()
    if (m < 0 || (m === 0 && now.getDate() < birth.getDate())) age--
    return age
}
```

#### 9.3.2 交互设计

- **触发时机**：身高、体重、性别、生日四个字段都有值时自动计算
- **显示样式**：体脂率输入框 placeholder 显示浅灰色"建议 xx.x%"，用户可点击一键填入或手动修改
- **关键提示**：标注"基于 BMI 估算，仅供参考，准确值请使用体脂秤"
- **可编辑性**：用户可手动覆盖估算值（例如有体脂秤测量结果）

#### 9.3.3 适用场景

- **PC 端个人中心**：基本资料 Tab（BasicTab.vue）中的体脂率字段
- **移动端个人中心**：身体数据录入页面的体脂率字段

### 9.4 后端职责边界

后端只负责：
- 接收用户**最终确认**的 bodyFatPct 值并存入 user_profile
- 校验 bodyFatPct 范围（3-60%，已通过 @DecimalMin/@DecimalMax 实现于 [UpdateProfileReq.java](file:///d:/FitPulse/fitness-backend/src/main/java/com/fitpulse/app/user/dto/req/UpdateProfileReq.java)）

估算公式的逻辑放后端反而会增加不必要的 API 调用和延迟，且公式简单到不值得抽成接口。

### 9.5 公式局限性说明

- BMI 法估算体脂率精度有限（±3-5%误差），仅作为用户填写参考
- 对运动员、孕妇、老年人等特殊群体误差较大
- 真正的体脂率应通过体脂秤、皮褶卡尺、DEXA 等方式测量
- 前端需明确标注"仅供参考"字样，避免误导

---

## 十、File 模块最小实现 P4（2026-08-19 完成）

### 10.1 文件清单

| 文件 | 类型 | 说明 |
|---|---|---|
| `entity/FileResource.java` | 新建 | 文件资源实体（对应 file_resource 表） |
| `mapper/FileResourceMapper.java` | 新建 | 文件资源 Mapper |
| `file/service/FileStorageService.java` | 新建 | 【策略模式】文件存储抽象接口 |
| `file/service/impl/LocalFileStorageServiceImpl.java` | 新建 | 【策略模式-具体策略】本地磁盘存储实现 |
| `file/service/FileService.java` | 新建 | 【门面模式】业务接口 |
| `file/service/impl/FileServiceImpl.java` | 新建 | 业务实现：存储 + 写 DB |
| `file/controller/FileController.java` | 新建 | POST /api/v1/file/upload 端点 |
| `file/dto/vo/FileUploadVO.java` | 新建 | 上传响应 VO |
| `file/enums/FileErrorCode.java` | 新建 | 5 个枚举值（400/500 大类） |
| `common/config/WebMvcConfig.java` | 新建 | 静态资源映射 /files/** → 本地磁盘 |
| `application.yml` | 修改 | 新增 fitpulse.storage 配置段 |
| `application-demo.yml` | 修改 | 同步 storage 配置占位 |
| `application-dev.yml` | 修改 | 同步 storage 配置（type=local，path=D:/FitPulseData/files） |

### 10.2 设计模式与编码技巧

#### 策略模式（Strategy Pattern）

**位置**：[FileStorageService.java](file:///d:/FitPulse/fitness-backend/src/main/java/com/fitpulse/app/file/service/FileStorageService.java) + [LocalFileStorageServiceImpl.java](file:///d:/FitPulse/fitness-backend/src/main/java/com/fitpulse/app/file/service/impl/LocalFileStorageServiceImpl.java)

**学习点**：
- 将"文件存储"这一行为抽象为接口，不同实现代表不同存储策略
- 当前只有本地存储实现，未来新增 MinIO 实现只需新增一个类，不改现有代码
- 符合"开闭原则"（对扩展开放，对修改关闭）
- FileService 面向接口注入 FileStorageService，切换实现只需改配置

```java
// 接口（抽象策略）：
public interface FileStorageService {
    FileResource store(MultipartFile file, String bucket, Long userId);
}

// 本地实现（具体策略 1）：
@Service
public class LocalFileStorageServiceImpl implements FileStorageService { ... }

// 未来 MinIO 实现只需新增（具体策略 2）：
// @Service
// @ConditionalOnProperty(name = "fitpulse.storage.type", havingValue = "minio")
// public class MinioFileStorageServiceImpl implements FileStorageService { ... }
```

#### 门面模式（Facade Pattern）思想

**位置**：[FileServiceImpl.java](file:///d:/FitPulse/fitness-backend/src/main/java/com/fitpulse/app/file/service/impl/FileServiceImpl.java)

**学习点**：
- FileService 作为业务门面，内部组合"存储 + 写 DB"两步操作
- 对 Controller 屏蔽底层存储细节（Controller 不知道是本地还是 MinIO）
- 门面模式简化了客户端调用，Controller 只需调一个方法

```java
// Controller 只调一个方法，不关心存储细节：
fileService.upload(file, bucket, userId);

// FileServiceImpl 内部组合两步：
FileResource fileResource = fileStorageService.store(file, bucket, userId);  // 委托存储
fileResourceMapper.insert(fileResource);                                      // 写 DB
```

#### 路径穿越攻击防御

**位置**：[LocalFileStorageServiceImpl.java#validateBucket](file:///d:/FitPulse/fitness-backend/src/main/java/com/fitpulse/app/file/service/impl/LocalFileStorageServiceImpl.java)

**学习点**：
- bucket 参数直接拼接到文件路径，如果不校验，攻击者可传 `../../etc` 之类的恶意值
- 使用白名单（Set.contains）而非黑名单，更安全
- 文件扩展名同样用白名单校验，防止上传 .exe/.jsp 等危险文件

```java
// 白名单校验，防止路径穿越：
private static final Set<String> ALLOWED_BUCKETS = Set.of("avatar", "exercise", "food", "general");

private void validateBucket(String bucket) {
    if (bucket == null || !ALLOWED_BUCKETS.contains(bucket)) {
        throw new BusinessException(FileErrorCode.BUCKET_INVALID);
    }
}
```

#### 静态资源映射的性能技巧

**位置**：[WebMvcConfig.java](file:///d:/FitPulse/fitness-backend/src/main/java/com/fitpulse/app/common/config/WebMvcConfig.java)

**学习点**：
- 使用 @Configuration + WebMvcConfigurer 而非 @Controller 处理静态文件
- 原因：WebMvcConfigurer 配置的静态资源由 ResourceHttpRequestHandler 直接处理，不进入 DispatcherServlet 的控制器扫描链，性能最优
- `file:` 前缀告诉 Spring 这是文件系统路径，结尾必须带 `/`

#### 编译错误修复：三元表达式优先级陷阱

**位置**：[WebMvcConfig.java#L30](file:///d:/FitPulse/fitness-backend/src/main/java/com/fitpulse/app/common/config/WebMvcConfig.java#L30)

**学习点**：
- 错误写法 `"file:" + path.endsWith("/") ? path : path + "/"` 会被解析为 `("file:" + boolean) ? path : path + "/"`
- 因为 `+` 的优先级高于 `?:`，导致 String + boolean 类型不兼容
- 正确写法：先计算三元表达式，再拼接前缀

```java
// 错误（编译失败）：
String location = "file:" + uploadPath.endsWith("/") ? uploadPath : uploadPath + "/";

// 正确（先三元，后拼接）：
String base = uploadPath.endsWith("/") ? uploadPath : uploadPath + "/";
String location = "file:" + base;
```

### 10.3 文件上传流程

```
前端 multipart/form-data POST /api/v1/file/upload
  ├─ file: 二进制文件
  └─ bucket: "avatar" | "exercise" | "food" | "general"
    │
    ▼
FileController.upload()
    │  从 SecurityContext 取 userId
    ▼
FileServiceImpl.upload()
    │  1. 委托 fileStorageService.store() 落盘
    │     - 校验文件非空、bucket 白名单、扩展名白名单
    │     - 生成相对路径：{bucket}/{yyyy/MM/dd}/{uuid}.{ext}
    │     - 写入磁盘：{upload-path}/{相对路径}
    │     - 生成 URL：/files/{相对路径}
    │  2. 写入 file_resource 表
    │  3. 返回 FileUploadVO {id, fileUrl}
    ▼
前端拿到 fileUrl，后续在业务表（如 user_profile.avatar_url）中保存关联
```

### 10.4 配置说明

**application.yml（公共）**：
```yaml
fitpulse:
  storage:
    type: local                          # local=本地磁盘降级 / minio=MinIO 对象存储
    upload-path: D:/FitPulseData/files   # 本地存储根路径
```

**个人版使用方式**：
- 无需安装 MinIO
- 在本地创建 `D:/FitPulseData/files/` 目录（或修改 upload-path）
- 文件通过 `http://localhost:8080/files/{bucket}/yyyy/MM/dd/xxx.jpg` 直接访问

### 10.5 编译验证

- `mvn compile` 通过（exit code 0）
- 首次编译失败：WebMvcConfig 第30行三元表达式优先级陷阱 → 修正后通过

### 10.6 git 提交

P4 合并提交。

---

## 十一、MinIO 存储策略扩展 P4+（2026-08-19 完成）

### 11.1 目标

在 P4 已有本地存储实现的基础上，新增 MinIO 对象存储方案，通过修改 yml 配置即可切换存储策略，**无需修改任何业务代码**。

### 11.2 文件清单

| 文件 | 类型 | 说明 |
|---|---|---|
| `pom.xml` | 已存在 | MinIO SDK 8.5.7 已在 P1 阶段引入，无需新增 |
| `common/config/MinioProperties.java` | 新建 | @ConfigurationProperties(prefix="fitpulse.minio") |
| `common/config/MinioConfig.java` | 新建 | @ConditionalOnProperty 控制 Bean 装配 + @PostConstruct 自动建桶 |
| `file/service/impl/LocalFileStorageServiceImpl.java` | 修改 | 加 @ConditionalOnProperty(havingValue="local", matchIfMissing=true) |
| `file/service/impl/MinioFileStorageServiceImpl.java` | 新建 | MinIO 存储实现 |
| `application.yml` | 修改 | 完善 minio 配置占位 |
| `application-demo.yml` / `application-dev.yml` | 已对齐 | minio 段落已在 P4 阶段预置 |

### 11.3 设计模式与编码技巧

#### 条件装配（@ConditionalOnProperty）

**位置**：[LocalFileStorageServiceImpl.java#L32](file:///d:/FitPulse/fitness-backend/src/main/java/com/fitpulse/app/file/service/impl/LocalFileStorageServiceImpl.java#L32) 和 [MinioFileStorageServiceImpl.java](file:///d:/FitPulse/fitness-backend/src/main/java/com/fitpulse/app/file/service/impl/MinioFileStorageServiceImpl.java) 和 [MinioConfig.java](file:///d:/FitPulse/fitness-backend/src/main/java/com/fitpulse/app/common/config/MinioConfig.java)

**学习点**：
- Spring Boot 的 @ConditionalOnProperty 实现"配置驱动的 Bean 装配"
- 当 yml 配置 `fitpulse.storage.type=local` 时，LocalFileStorageServiceImpl 生效
- 当 yml 配置 `fitpulse.storage.type=minio` 时，MinioFileStorageServiceImpl + MinioConfig 生效
- `matchIfMissing=true` 表示未配置时默认激活 local 实现（个人版开箱即用）
- 两个实现类互斥，同一时刻只有一个 Bean 被创建，避免冲突
- FileService 通过接口注入 FileStorageService，Spring 自动装配生效的那个实现

```java
// 默认激活本地实现：
@Service
@ConditionalOnProperty(name = "fitpulse.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageServiceImpl implements FileStorageService { ... }

// 配置 type=minio 时才激活 MinIO 实现：
@Service
@ConditionalOnProperty(name = "fitpulse.storage.type", havingValue = "minio")
public class MinioFileStorageServiceImpl implements FileStorageService { ... }

// 配置 type=minio 时才创建 MinioClient Bean：
@Configuration
@ConditionalOnProperty(name = "fitpulse.storage.type", havingValue = "minio")
public class MinioConfig {
    @Bean
    public MinioClient minioClient() { ... }
}
```

#### Bucket 自动创建（@PostConstruct 容错设计）

**位置**：[MinioConfig.java#initBucket](file:///d:/FitPulse/fitness-backend/src/main/java/com/fitpulse/app/common/config/MinioConfig.java)

**学习点**：
- @PostConstruct 在 Bean 初始化完成后立即执行，适合做"启动时检查"
- 容错设计：首次启动自动建桶，避免运维手动操作
- 如果 MinIO 服务未启动，启动会失败并给出明确错误日志

#### 策略模式完整闭环

**学习点**：
- P4 阶段只有"抽象接口 + 一个具体策略（本地）"
- 本阶段新增"另一个具体策略（MinIO）"，策略模式完整闭环
- 未来扩展 OSS/COS 只需新增一个实现类 + 一个配置类，不改任何现有代码
- 这就是"开闭原则"的最佳体现：对扩展开放，对修改关闭

### 11.4 配置切换方式

**切换到本地存储（默认，个人版推荐）**：
```yaml
fitpulse:
  storage:
    type: local
    upload-path: D:/FitPulseData/files
```

**切换到 MinIO 存储（生产环境）**：
```yaml
fitpulse:
  storage:
    type: minio
  minio:
    endpoint: http://127.0.0.1:9000
    access-key: minioadmin
    secret-key: minioadmin
    bucket: fitpulse-assets
    public-base-url: http://127.0.0.1:9000/fitpulse-assets
```

**未配置 type 时**：默认激活 local 实现（matchIfMissing=true），个人版无需任何配置即可使用。

### 11.5 编译验证

- `mvn compile` 通过（exit code 0）
- 无新增编译错误

### 11.6 git 提交

P4+ 合并提交。

---

## 十、个人中心资料卡视觉重构与体脂率估算实现（2026-08-19）

### 10.1 重构目标

原有 `BasicTab.vue`（个人中心-基本资料）布局为朴素的单列表单，视觉层次单薄，缺乏品牌感。本次重构在不改变数据结构和接口的前提下，通过 **卡片式分区布局** 提升视觉美感与信息组织度，并同步落地 §九 中已决策的体脂率前端估算功能。

### 10.2 视觉分区设计

将原单列表单拆分为 4 个视觉区块，每个区块均采用带阴影、圆角的独立卡片：

| 区块 | 视觉特征 | 承载内容 |
|---|---|---|
| **个人名片卡** | 渐变光晕背景 + 圆形大头像 + 等级徽章 Tag + 芯片式元信息 | 头像、昵称、健身等级、个人简介、性别/生日/身高 chip |
| **基本信息区** | 白色卡片 + 栅格表单（2 列） + 品牌色分段控件 | 昵称、性别（分段控件）、生日、健身等级、简介 |
| **身体数据区** | A 紫色分区标识 | 身高、体重、体脂率（含估算交互） |
| **训练目标区** | C 绿色分区标识 + 独立保存按钮 | 目标类型、体重/体脂目标、训练频率、营养目标、日期 |

**卡片样式要点**：
- 主卡片圆角 `16-18px`，阴影使用 `--shadow-soft` CSS 变量
- 区块头部带 icon + 标题 + 右对齐说明文字，底部带分隔线
- 表单使用 CSS Grid `grid-template-columns: repeat(2, 1fr)` 自动 2 列
- 分段控件（性别）使用品牌紫 `--fit-brand` 高亮，选中时带投影
- 响应式：720px 以下自动切换为单列

### 10.3 体脂率估算交互实现

按 §九 决策，在 `BasicTab.vue` 中实现 Deurenberg 公式前端估算：

**触发条件**（全部满足时实时计算）：
- 身高 `heightCm` > 0
- 体重 `weightKg` > 0
- 性别 `gender` ∈ {1, 2}（男/女）
- 生日 `birthday` 有效，且年龄在 10-100 岁之间

**交互细节**：
- 体脂率输入框的 `placeholder` 动态显示 `建议 xx.x%（由 BMI 估算）`
- 当估算值与当前手填值不同时，显示 `填入 xx.x%` 一键填入按钮
- 估算下方显示提示行：`💡 根据身高 Xcm / 体重 Ykg / 性别 / Z岁 估算`
- 始终显示免责提示条：`⚠️ 基于 BMI 法估算，仅供参考。准确值请使用体脂秤测量。`（虚边框 + 柔和背景）
- 限制范围 3-60%，超出自动 clamp

**数据流向**：
```
用户输入身高/体重/性别/生日
  → computed(estimatedBodyFat) 实时计算
  → computed(bodyFatPlaceholder) 更新 placeholder
  → 显示填入按钮 + 提示行
  → 用户点击"填入" → form.bodyFatPct = estimatedBodyFat
  → 保存时 bodyFatPct 随 profile 一起提交到后端
```

### 10.4 实现文件

| 文件 | 变更类型 | 说明 |
|---|---|---|
| `fitness-web-admin/src/views/profile/tabs/BasicTab.vue` | 重构 | 模板重写为卡片分区，新增估算逻辑、交互 UI 和全部样式 |
| `fitness-web-admin/src/mock/user.js` | 复用 | 无需改动，已有 weightKg/bodyFatPct/fitnessLevel 字段 |
| `fitness-web-admin/src/api/user.js` | 复用 | 无需改动，updateMyProfile 已接收 bodyFatPct |

### 10.5 后续接入真实后端

- 当前 mock 已返回 `bodyFatPct` 初始值，用户填入的估算值会通过 `updateMyProfile` 提交到 `PUT /api/v1/user/profile`
- 后端 `UpdateProfileReq.bodyFatPct` 已有 `@DecimalMin("3") @DecimalMax("60")` 范围校验
- 切换真实后端：`.env` 设 `VITE_USE_MOCK=false` 即可，无需改动前端业务代码



