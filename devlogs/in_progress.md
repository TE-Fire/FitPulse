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

