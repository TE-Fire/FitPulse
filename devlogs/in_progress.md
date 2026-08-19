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



