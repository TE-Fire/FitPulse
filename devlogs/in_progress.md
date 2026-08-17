# FitPulse 开发工作记录（进行中）

> 会话日期：2026-08-17
> 当前状态：进行中
> 会话结束时将结合历史概括并重命名为 `yyyyMMdd_准确概括.md`

---

## 本次会话任务
1. JDK 17 → 21 升级 + Maven 多仓库源配置（阿里云 public / 阿里云 spring / Spring Milestones / Central fallback）
2. pom.xml 依赖审查（修复空 version 标签 + 补齐插件显式版本）
3. Java 代码局部变量 `var` → 显式类型
4. 建立工作记录机制：`D:\FitPulse\devlogs\in_progress.md`（会话结束时根据历史重命名）
5. 清空后端 Java 源码，等待用户按模块指示逐步重建
6. 按设计契约生成接口文档两份（md阅读版 + OpenAPI3.0.3 Apifox版）

---

## 1. pom.xml：JDK 21 升级
- `<java.version>`: 17 → **21**
- 新增属性：
  - `<maven.compiler.release>21</maven.compiler.release>`
  - `<maven.compiler.source>21</maven.compiler.source>`
  - `<maven.compiler.target>21</maven.compiler.target>`
  - `<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>`

## 2. pom.xml：多仓库源（3+1 fallback，含国内阿里云）
### repositories（按顺序解析）
1. **aliyun-public** `https://maven.aliyun.com/repository/public`
2. **aliyun-spring** `https://maven.aliyun.com/repository/spring`
3. **spring-milestones** `https://repo.spring.io/milestone`（Spring AI 需要里程碑仓库）
4. **maven-central** `https://repo1.maven.org/maven2`（兜底）

### pluginRepositories
同构配置 aliyun-plugin-public / spring-plugin-milestones / maven-central-plugin

## 3. pom.xml：依赖审查与修复（发现并修复高危问题）
### 🔴 原问题：7 个依赖 `<version></version>` 标签为空（Maven 无法解析）
修复：填入对应的 properties 变量
| 依赖 | 版本 |
|---|---|
| jjwt-api / jjwt-impl / jjwt-jackson | 0.12.5 |
| mybatis-plus-spring-boot3-starter | 3.5.5 |
| minio | 8.5.7 |
| spring-ai-openai-spring-boot-starter | 1.0.0-M3 |
| hutool-all | 5.8.25 |

### 插件：消除 build.plugins.plugin.version missing 告警
- **maven-compiler-plugin:3.13.0** 放入 `<pluginManagement>`，参数 release=21 / parameters=true
- spring-boot-maven-plugin 继承 parent

### 去重/多余 version 检查：
- 无重复 GAV
- Spring Boot BOM 管理的依赖未重复写 version ✓

---

## 4. Java：`var` → 显式类型（共 5 处，2 个文件）
| 文件 | 替换 |
|---|---|
| auth/jwt/CurrentUser.java | SecurityContext ctx / Object p / JwtAuthFilter.LoginUser u |
| auth/jwt/JwtAuthFilter.java | Claims claims / UsernamePasswordAuthenticationToken authToken |

验证：grep 全目录 → No matches ✓

---

## 5. 工作记录机制
- 目录：`D:\FitPulse\devlogs\`（用户选方案 C）
- 临时：`in_progress.md`；会话结束你喊停时重命名为 `yyyyMMdd_<概括>.md`

---

## 6. 清空后端 Java 源码（等待按模块逐个重建）
- 删除范围：`fitness-backend/src/main/java/**/*.java`（共 30 个文件）
- 分组：
  - common层：Result/ResultCode/PageResult + 两个异常类 + Security/Cors/MyBatisPlus/Redis/Minio/AiPromptConfig（共 12）
  - auth/jwt：JwtProperties/JwtTokenProvider/JwtAuthFilter/CurrentUser（共 4）
  - FitnessApplication：1
  - 业务层：DashboardController, AiController+AiService, FileController+FileStorageService, UserController, TrainingController+4Mapper, HealthController+3Mapper（共 13）
- 保留：pom.xml / resources/**（application.yml/sql/logback）/ 空包目录结构
- 校验：Remaining .java files: 0 ✓

---

## 7. 按设计契约生成接口文档两份
### 7.1 阅读版 `docs/接口文档.md`（16554字）
章节结构：
1. 基础约定（统一 Result / 状态码表 / 分页格式 / 鉴权 401 → refresh → 登录流程）
2. 认证 `/auth`：register / login / refresh / logout（每接口：请求表+JSON + 响应表+JSON）
3. 看板 `/admin/dashboard`：training（B/C 高亮字段）、health（A/B 高亮字段）
4. 训练 `/training`：动作库(4) + 计划(4) + 记录(3) = 11 接口
5. 健康 `/health`：身体指标(3) + 食物库(2) + 饮食(3) + 饮水(3) = 11 接口
6. 文件 `/file/upload`：multipart/form-data（bucket=avatar/exercise/food/general）
7. AI `/ai`：chat / advise-training-plan / advise-meal（3 接口，Markdown 响应）
8. 用户 `/user`：me(GET/PUT) / password / goal(GET/PUT) = 5 接口
9. 附录 A 枚举字典（9 类） + 附录 B 文件目录约定（4 bucket）

### 7.2 ApiFox 版 `docs/接口文档_apifox.json`（OpenAPI 3.0.3，59690字）
- 已做 JSON 语法自检 → **OK** ✓
- 核心结构：
  - `openapi: 3.0.3`
  - `servers[0]`: `http://localhost:8080/api/v1`（ApiFox 导入后直接点"测试"即可）
  - `tags`: 7 模块（与 md 分组一致）
  - `security`: BearerAuth（http+bearer+JWT，**全局默认**，3 个免鉴权接口单独 `security:[]` 覆盖）
  - `paths`: **39** 条接口（register/login/refresh/logout + 2dashboard + training11 + health11 + file1 + ai3 + user5）
  - `components.parameters`: 复用 query pageNum/pageSize / path id
  - `components.schemas`: **40+** Schema 可引用（Result / Page / 全部 Req & VO）；TrainingOverview 明确标注【B/C】、HealthOverview【A/B】
- 导入方式：ApiFox → 项目 → 导入 → OpenAPI/Swagger → 选择文件 → 自动生成"运行"用例 + "文档"

---

> 会话未结束，等待用户按模块逐一重建后端 Java 代码
---

## 8. Auth 接口修订（QQ邮箱注册 + 合并登录 + 验证码 + 鉴权策略调整）

### 8.1 用户需求
1. 注册使用 QQ 邮箱（@qq.com），不再要求 username（后端取邮箱前缀自动生成）
2. 登录合并为单一接口，通过 	ype 字段区分：1=密码登录 2=验证码登录
3. 验证码通过日志打印 + QQ 邮箱 SMTP 发送
4. 测试阶段不强制鉴权凭证（ApiFox JSON 去掉全局 security，改为单接口单独标注）

### 8.2 auth 接口最终 5 条（原 4 条 → 5 条）

| # | 方法 | 路径 | 鉴权 | 请求体 | 响应 data |
|---|---|---|---|---|---|
| 1 | POST | /auth/register | 否 | {email(@qq.com), password} | null |
| 2 | POST | /auth/login | 否 | {email, type(1|2), password?, code?} | {accessToken, refreshToken, userId, username} |
| 3 | POST | /auth/login/send-code | 否 | {email} | null（6位码5min，日志+SMTP） |
| 4 | POST | /auth/refresh | 否 | {refreshToken} | 同 login（旋转失效） |
| 5 | POST | /auth/logout | 是 | 无 Body | null |

### 8.3 三份文件同步更新

#### docs/接口文档.md
- 第2章认证模块完全重写：5 条接口，每条含请求字段表+JSON示例+响应字段表+JSON示例
- register：email 必填 @qq.com + password 8-64
- login：type=1 需 password，type=2 需 code（6位数字）
- send-code：服务端行为4步（生成→Redis→日志→SMTP），429防刷60s

#### docs/接口文档_apifox.json（OpenAPI 3.0.3）
- 删除全局 security，改为每个接口单独标注
  - 4 个免鉴权接口：register / login / login/send-code / refresh → security: []
  - 36 个需鉴权接口 → security: [{BearerAuth: []}]
  - ApiFox 导入后父级不再强制继承 Bearer Token，免鉴权接口可直接点测试
- 新增 path：/auth/login/send-code
- 修改 schemas：
  - RegisterReq：{email(pattern=@qq.com), password}
  - LoginReq：{email, type(enum[1,2]), password?, code?}
  - SendCodeReq（新增）：{email}
- JSON 语法自检通过

#### docs/设计契约.md
- 6.1 节接口表从 4 条更新为 5 条，字段与新接口文档严格一致

### 8.4 下一会话
等待用户指示按模块逐步重建后端 Java 代码

---

## 9. P0 启动阻塞项落地（2026-08-17 新会话）

### 9.1 pom.xml 补充依赖
- 追加 `spring-boot-starter-mail`（位置：Redis 之后、MinIO 之前）

### 9.2 application.yml 两处修正
- JWT access 过期时间：`10080`（7天）→ `1440`（24小时，与接口文档 1.4 节对齐）
- 新增 `fitpulse.mail` 配置块：QQ SMTP（smtp.qq.com:465），用户名/密码留空待填，无凭据时 MailService 仅日志不报错

### 9.3 common 模块基础代码（共 14 个 Java 文件）

| 包 | 文件 | 说明 |
|---|---|---|
| 根 | FitnessApplication.java | 启动类：@SpringBootApplication + @EnableAsync + @ConfigurationPropertiesScan + @MapperScan |
| common/result | IErrorCode.java | 错误码接口 |
| common/result | ResultCode.java | 枚举：SUCCESS/PARAM_ERROR/UNAUTHORIZED/FORBIDDEN/NOT_FOUND/CONFLICT/INTERNAL_ERROR |
| common/result | Result.java | 统一响应包裹：code/message/data/timestamp |
| common/result | PageResult.java | 分页结构：total/pageNum/pageSize/pages/list |
| common/exception | BaseException.java | 基础异常，实现 IErrorCode |
| common/exception | BusinessException.java | 业务异常，继承 BaseException |
| common/exception | GlobalExceptionHandler.java | @RestControllerAdvice：处理 BusinessException/ValidationException/AuthException/兜底 Exception |
| common/enums | LoginTypeEnum.java | PASSWORD(1)/VERIFY_CODE(2)，fromCode 静态方法 |
| common/constants | RedisKeyConstants.java | fitpulse: 前缀 + buildKey 工具方法 |
| common/config | RedisConfig.java | Jackson2JsonRedisSerializer 序列化配置 |
| common/config | MyBatisPlusConfig.java | MetaObjectHandler 自动填充 createdAt/updatedAt |
| common/config | MailProperties.java | @ConfigurationProperties(prefix="fitpulse.mail")，isConfigured() 判断 |
| common/mail | MailService.java | 邮件发送，无凭据仅日志不报错，SSL 465 端口 |

### 9.4 下一步
准备执行 P1：Auth 鉴权模块（实体+Mapper+JWT+Security+Controller）