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

---

## 10. P0 代码规范微调（3 处）

### 10.1 ResultCode → ErrorCodeEnum
- **位置迁移**：`common/result/ResultCode.java`（删除）→ `common/enums/ErrorCodeEnum.java`（新建，implements IErrorCode）
- **同步引用**（共 4 处）：
  - `Result.java`：`ResultCode.SUCCESS` → `ErrorCodeEnum.SUCCESS`
  - `BusinessException.java`：构造参数 `ResultCode` → `ErrorCodeEnum`
  - `GlobalExceptionHandler.java`：所有 `ResultCode.XXX` → `ErrorCodeEnum.XXX`（PARAM_ERROR/UNAUTHORIZED/INTERNAL_ERROR 等）

### 10.2 LoginTypeEnum 构造用 Lombok 注解
- 删除手写构造器，类上追加 `@AllArgsConstructor`
- `@Getter`、`fromCode()` 静态方法保持不变

### 10.3 RedisKeyConstants 分场景 buildKey 方法
- 删除可变参数方法 `buildKey(String... segments)`
- LOGIN_CODE / REFRESH_TOKEN 常量改为 `private`（仅供内部格式化使用）
- 新增两个静态方法：
  - `buildLoginCodeKey(String email)` → `fitpulse:login:code:{email}`
  - `buildRefreshTokenKey(String userId)` → `fitpulse:auth:refresh:{userId}`

---

## 11. 异常契约接口重构：对齐参考项目规范（IErrorCode → BaseExceptionInterface）

### 11.1 接口替换
- **删除**：`common/result/IErrorCode.java`
- **新建**：`common/exception/BaseExceptionInterface.java`（参考 BlueNoteBook 规范）
  - `String getErrorCode()` // 获取异常码
  - `String getErrorMessage()` // 获取异常信息

### 11.2 Result 结构 code 字段：int → String（全链路统一）
| 位置 | 变更前 | 变更后 |
|---|---|---|
| Result.java 字段 | `private int code` | `private String code` |
| Result.fail(int, String) 兜底签名 | `fail(int code, String message)` | `fail(String code, String message)` |
| Result.success() 取成功码 | `ResultCode.SUCCESS.getCode()`（int） | `ErrorCodeEnum.SUCCESS.getErrorCode()`（String "0"） |

### 11.3 异常类联动修改
- **ErrorCodeEnum**：`private final int code` → `String code`；常量值 `"0"/"400"/"401"...`；实现 `BaseExceptionInterface`，提供 `getErrorCode()`/`getErrorMessage()`
- **BaseException**：`private final int code` → `String code`；构造签名同步；`implements BaseExceptionInterface`
- **BusinessException**：构造参数类型自动适配新签名

### 11.4 Result 新增 fail(BusinessException) 重载 + Handler 简化
- Result.java 新增 `fail(BusinessException e)`：直接取 e.getErrorCode()/e.getErrorMessage() 组装返回
- GlobalExceptionHandler.handleBusinessException 简化：`Result.fail(e.code(), e.getMessage())` → `return Result.fail(e);`

---

## 12. P1-1 Auth模块实体层精简落地（仅User表，注册登录用）

> 按阶段最小化原则：P1 只关注注册登录流程，UserProfile / UserGoal 延后到个人资料/目标模块

### 12.1 文件清单（2 个）
| 文件 | 说明 |
|---|---|
| `entity/User.java` | 表 user，雪花ID（ASSIGN_ID），字段：username / passwordHash / email / status(Integer 1启用/0禁用) / lastLoginAt / createdAt(INSERT填充) / updatedAt(INSERT_UPDATE填充) / deleted(@TableLogic) |
| `mapper/UserMapper.java` | `@Mapper`，继承 `BaseMapper<User>`，无业务SQL（行级隔离在Service传参） |

### 12.2 阶段剔除清单（延后非P1）
- ❌ entity/UserProfile.java
- ❌ entity/UserGoal.java
- ❌ mapper/UserProfileMapper.java
- ❌ mapper/UserGoalMapper.java
- ❌ AuthService.register 中 Profile/Goal 初始化逻辑（后续模块创建时补）

---

## 13. P1-2 JWT 层落地（4 件套）

> 基于 jjwt 0.12.5（HS256 对称签名），refreshToken 双写 Redis 实现服务端失效控制。

### 13.1 文件清单（4 个）
| 文件 | 说明 |
|---|---|
| `auth/jwt/JwtProperties.java` | `@ConfigurationProperties(prefix="fitpulse.jwt")`，字段：secret / accessExpireMinutes(1440) / refreshExpireMinutes(43200) / header(Authorization) / prefix("Bearer ") |
| `auth/jwt/JwtTokenProvider.java` | `@Component`，`@PostConstruct` 生成 SecretKey。<br>• `generateAccessToken(userId, username)` — claim: sub=userId, username, type=access<br>• `generateRefreshToken(userId)` — claim: type=refresh，同时写入 Redis key=`fitpulse:auth:refresh:{userId}`，TTL=refreshExpire<br>• `parseToken(token)` — `Jwts.parser().verifyWith(key).build().parseSignedClaims()`，失败抛 BusinessException(UNAUTHORIZED)<br>• `getUserIdFromToken(token)` — 解析 sub 转 Long<br>• `isRefreshTokenValid(userId, token)` — Redis 比对<br>• `revokeRefreshToken(userId)` — 删除 Redis key |
| `auth/jwt/JwtAuthFilter.java` | `@Component`，继承 `OncePerRequestFilter`。<br>从 Header 取 Authorization 去前缀 → parseToken → 校验 type=access → 构造 `UsernamePasswordAuthenticationToken(userId, null, emptyList)`，`setDetails(username)` → 写入 SecurityContext。<br>约定：**principal=userId(Long), details=username(String)**。<br>异常不抛，仅清 Context 放行。 |
| `auth/jwt/CurrentUser.java` | 静态工具类（private 构造）。<br>• `getUserId()` — 从 principal 取 Long，未登录返回 null<br>• `getUsername()` — 从 details 取 String，未登录返回 null |

### 13.2 关键设计点
- **jjwt 0.12.x API**：`Jwts.builder().subject().claim().signWith(key).compact()` / `Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload()`
- **token 类型隔离**：access/refresh 用 `type` claim 区分，filter 仅认 access 类型，防止 refresh 被滥用为鉴权 token
- **refresh Redis 双写**：生成即写，登出/旋转即删，实现服务端主动失效
- **Filter 异常策略**：解析失败清 Context 放行，由 Security 链的 anyRequest().authenticated() + RestAuthenticationEntryPoint 统一返回 401（P1-3 落地）

---

## 14. P1-3 Spring Security 配置落地（3 个类）

> 无状态 JWT 模式：禁用 CSRF/Session，CORS 开发期全放行，白名单放行 auth 公开接口，JwtAuthFilter 注入过滤器链。

### 14.1 文件清单（3 个）
| 文件 | 说明 |
|---|---|
| `security/RestAuthenticationEntryPoint.java` | `@Component`，implements `AuthenticationEntryPoint`。未认证访问受保护资源时触发（401），用 ObjectMapper 写 JSON `Result.fail(UNAUTHORIZED)`，response.setStatus(401) |
| `security/RestAccessDeniedHandler.java` | `@Component`，implements `AccessDeniedHandler`。已认证但无权限时触发（403），写 JSON `Result.fail(FORBIDDEN)`，response.setStatus(403) |
| `security/SecurityConfig.java` | `@Configuration` + `@EnableWebSecurity` + `@EnableMethodSecurity`<br>• csrf.disable / sessionManagement(STATELESS)<br>• CORS：allowedOriginPatterns=`*`，allowedMethods=GET/POST/PUT/DELETE/PATCH/OPTIONS，allowCredentials=true，maxAge=3600<br>• 白名单（permitAll）：`/api/v1/auth/register`、`/login`、`/login/send-code`、`/refresh`、`/error`、`/actuator/**`，OPTIONS 全放行<br>• anyRequest().authenticated()<br>• exceptionHandler 绑定 RestAuthenticationEntryPoint + RestAccessDeniedHandler<br>• addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)<br>• Bean：PasswordEncoder → BCryptPasswordEncoder |

### 14.2 关键设计点
- **白名单精确到接口路径**：4 个 auth 公开接口 + error + actuator，其余全部 authenticated()
- **OPTIONS 预检放行**：`requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()`，避免浏览器 CORS 预检被 401 拦截
- **异常处理 JSON 化**：EntryPoint + DeniedHandler 统一返回 Result JSON，替代 Spring Security 默认 HTML 登录页
- **MethodSecurity 开启**：`@EnableMethodSecurity` 预留 `@PreAuthorize` 注解能力（后续 RBAC 模块使用）

---

## 15. P1-5 DTO/VO 落地（5 个）

> 所有请求 DTO 加 Jakarta Validation 注解，Controller 层 `@Valid` 触发 GlobalExceptionHandler.handleValidation。

| 文件 | 关键字段 & 校验 |
|---|---|
| `dto/req/RegisterReq.java` | email: `@NotBlank`+`@Email`+`@Pattern(.+@qq\\.com$)`<br>password: `@NotBlank`+`@Size(8-64)`+`@Pattern(字母+数字)` |
| `dto/req/LoginReq.java` | email: `@NotBlank`+`@Pattern(.+@qq\\.com$)`<br>type: `@NotNull Integer`<br>password: `@Size(8-64)`（type=1时Service再校验）<br>code: `@Pattern(^\\d{6}$)`（type=2时Service再校验） |
| `dto/req/SendCodeReq.java` | email: `@NotBlank`+`@Pattern(.+@qq\\.com$)` |
| `dto/req/RefreshReq.java` | refreshToken: `@NotBlank` |
| `dto/vo/LoginUserVO.java` | `@Data @Builder @NoArgsConstructor @AllArgsConstructor`<br>字段：accessToken / refreshToken / userId / username |

---

## 16. P1-4 AuthService 业务层落地（5 方法）

> `@Service @RequiredArgsConstructor`，依赖：UserMapper / JwtTokenProvider / RedisTemplate / PasswordEncoder / MailService。
> P1 最小化：只操作 user 表 + Redis，UserProfile/UserGoal 初始化留到后续模块。

### 16.1 register(RegisterReq)
1. 邮箱唯一性检查：`userMapper.selectCount(eq(User::email, email))` > 0 → CONFLICT
2. 生成 username：从 email @ 前缀出发，冲突追加 2/3/... 直到唯一（上限 100 次，兜底 UUID8 位）
3. 插入 user：username / passwordHash(BCrypt.encode) / email / status=1 / lastLoginAt=null
4. **不自动登录**，void 返回

### 16.2 login(LoginReq) → LoginUserVO
1. type 用 `LoginTypeEnum.fromCode` 校验，null → PARAM_ERROR
2. 按 email 查 user：不存在 / status!=1 → UNAUTHORIZED（统一提示"邮箱或密码错误"防枚举）
3. 分支：
   - **PASSWORD**：`!passwordEncoder.matches(req.password, user.passwordHash)` → UNAUTHORIZED
   - **VERIFY_CODE**：Redis `buildLoginCodeKey(email)` 取验证码 → null=过期、不一致=错误，成功后 **立即 delete（一次性消费）**
4. 更新 `user.lastLoginAt=now()` → updateById
5. 生成双 Token：`generateAccessToken` + `generateRefreshToken`（Redis 写 refresh）
6. 组装 LoginUserVO 返回

### 16.3 sendCode(SendCodeReq)
1. **60s 防刷**：`hasKey(buildLoginCodeKey(email)+":rate")` → CONFLICT
2. 生成 6 位随机码：`(int)((Math.random()*9+1)*100000)`（确保首位非零）
3. Redis 双写：code(5min) + rate(60s)
4. `log.info` 输出验证码（开发调试用）
5. `mailService.sendMail(email, "FitPulse 登录验证码", content)` — MailService 内部判断凭据

### 16.4 refresh(RefreshReq) → LoginUserVO
1. parseToken(refreshToken)：失败 → UNAUTHORIZED("已失效请重新登录")；type != "refresh" → PARAM_ERROR
2. `isRefreshTokenValid(userId, token)` Redis 比对：不一致 → UNAUTHORIZED
3. selectById 查用户确认 status=1：禁用则 revoke 后抛 UNAUTHORIZED
4. **旋转**：`revokeRefreshToken(userId)` → 删除旧的，生成一对新双 Token 写 Redis
5. 返回 LoginUserVO

### 16.5 logout(Long userId)
- userId==null 直接返回
- `revokeRefreshToken(userId)` 即删 Redis key，下次 refresh 即失效

### 16.6 编译验证
`mvn compile` BUILD SUCCESS（JDK 21 / 29 个源文件 / 15.9s）

---

## 17. Auth 模块目录聚合重构：DTO/Service 迁入 auth 包下

### 17.1 重构背景
P1-5/6 初期把 DTO 放在全局 `dto/req`、`dto/vo`，Service 放在全局 `service/`。
根据项目约定，每个业务模块应有独立的 `dto`、`service` 子包，按模块聚合避免污染全局命名空间。

### 17.2 变更明细
| 原位置 | 新位置 |
|---|---|
| `dto/req/RegisterReq.java` | `auth/dto/req/RegisterReq.java` |
| `dto/req/LoginReq.java` | `auth/dto/req/LoginReq.java` |
| `dto/req/SendCodeReq.java` | `auth/dto/req/SendCodeReq.java` |
| `dto/req/RefreshReq.java` | `auth/dto/req/RefreshReq.java` |
| `dto/vo/LoginUserVO.java` | `auth/dto/vo/LoginUserVO.java` |
| `service/AuthService.java` | `auth/service/AuthService.java` |
| 空目录 `dto/`、`service/` | 删除 |

### 17.3 约定的目标结构（后续模块都遵循）
```
com.fitpulse.app/
├── auth/                 ← 每个业务模块独立包
│   ├── controller/       ← Controller 也放模块下（P1-6 迁入）
│   ├── service/          ← 本模块 Service
│   ├── dto/req/          ← 本模块请求 DTO
│   ├── dto/vo/           ← 本模块响应 VO
│   └── jwt/              ← 本模块专属工具（JWT 4 件套已在）
├── common/               ← 全局通用
├── entity/               ← 全局实体（跨模块共享）
├── mapper/               ← 全局 Mapper（跨模块共享）
└── security/             ← 全局 Security 配置
```

### 17.4 编译验证
`mvn compile` BUILD SUCCESS（JDK 21 / 29 个源文件 / 17.1s）

---

## 18. P1-6 AuthController 落地（5 个接口，完整链路贯通）

### 18.1 文件位置
`auth/controller/AuthController.java`
- `@RestController` + `@RequestMapping("/api/v1/auth")` + `@RequiredArgsConstructor`
- 依赖：`AuthService`

### 18.2 接口清单（与 SecurityConfig 白名单对齐）

| HTTP | 路径 | 鉴权 | 参数 (@Valid @RequestBody) | 返回 | 说明 |
|---|---|---|---|---|---|
| POST | `/register` | permitAll | `RegisterReq` | `Result<Void>` | 注册成功无返回体 |
| POST | `/login` | permitAll | `LoginReq` | `Result<LoginUserVO>` | 密码/验证码统一入口 |
| POST | `/login/send-code` | permitAll | `SendCodeReq` | `Result<Void>` | 60s 防刷在 Service 内 |
| POST | `/refresh` | permitAll | `RefreshReq` | `Result<LoginUserVO>` | refreshToken 旋转 |
| POST | `/logout` | authenticated | 无（从 CurrentUser 取） | `Result<Void>` | 删 Redis refreshToken |

### 18.3 关键设计
- **登出取 userId**：`CurrentUser.getUserId()` 读取 SecurityContext 的 principal（JwtAuthFilter 写入的 Long userId），若 Context 为空 → anyRequest().authenticated() 已在过滤器链拦截返回 401（所以 controller 内不需要再判空兜底，但 `authService.logout(null)` 内部也做了 null 安全）
- **全链路 @Valid**：请求 DTO 的 Jakarta Validation 注解（@NotBlank/@Pattern/@Size/@Email/@NotNull）全部生效，校验失败自动触发 `GlobalExceptionHandler.handleValidation`（返回 `Result.fail(PARAM_ERROR, message)`）
- **返回统一 Result**：成功 `Result.success()` 或 `Result.success(data)`，失败由 Service 抛 BusinessException → GlobalExceptionHandler 统一包裹 Result

### 18.4 编译验证
`mvn compile` BUILD SUCCESS（JDK 21 / 30 个源文件 / 16.1s）

---

## P1 阶段完成总结（截至第 18 节）

**Auth 模块 5 接口已全部可编译**：注册 → 发送验证码 → 登录（密码/验证码）→ 刷新 → 登出。
**运行前置条件**：MySQL 建 user 表、Redis 在线、（可选）邮件配置凭据填 application.yml。后续可本地启动后用 ApiFox 按文档链路验证。

---

## 19. AuthService 按「接口 + impl」分层（面向接口编程）

### 19.1 变更背景
P1-4 阶段 AuthService 直接写成具体类（`@Service` 注解在类上），不符合项目规范：
- Service 层目录下应只放**接口**，仅定义对外暴露的方法签名（契约）
- `service/impl/` 目录下放**实现类**，加 `@Service` 作为 Spring Bean
- Controller 只依赖**接口**（按类型自动装配实现类），解耦后可随时切换实现 / Mock

### 19.2 变更明细
| 原位置（单一具体类） | 新结构（接口+实现） |
|---|---|
| `auth/service/AuthService.java` 具体类 | 重写为 `interface AuthService`（5 抽象方法：register/login/sendCode/refresh/logout） |
| （无） | 新建 `auth/service/impl/AuthServiceImpl.java`<br>• `@Service @RequiredArgsConstructor implements AuthService`<br>• 所有公共方法加 `@Override`<br>• 私有辅助方法（resolveUniqueUsername、verifyPassword 等）保留在实现类中 |
| `AuthController` 中 `private final AuthService authService` | **无需修改代码**：字段类型现在指向接口，Spring IoC 按接口类型自动匹配唯一 Bean `AuthServiceImpl` |

### 19.3 约定的分层模板（后续所有业务模块都遵循）
```
{module}/
├── controller/           ← 依赖 {Module}Service（接口类型）
├── service/
│   ├── {Module}Service.java    ← 接口，方法签名契约
│   └── impl/
│       └── {Module}ServiceImpl.java  ← @Service implements，具体实现
├── dto/req/ & dto/vo/    ← 本模块专属 DTO
└── ...其他子包
```

### 19.4 编译验证
`mvn compile` BUILD SUCCESS（JDK 21 / 31 个源文件 / 14.8s）

---

## 20. Auth 模块专属业务错误枚举 + BusinessException 通用化构造器

### 20.1 重构背景
之前写法：`throw new BusinessException(ErrorCodeEnum.CONFLICT, "邮箱已注册")`
- 通用枚举的 message 是"资源冲突"，与第二个参数"邮箱已注册"重复且散
- 构造参数类型写死为 `ErrorCodeEnum`（具体类），其他模块无法传自定义枚举

### 20.2 改造点（2 个）

#### 20.2.1 新建 `auth/enums/AuthErrorCode`（11 个枚举常量）
- 实现 `BaseExceptionInterface`，与 common 异常契约兼容
- code 沿用 HTTP 大类值（400/401/409），前端仍可做大类判断
- 每个枚举精确对应 auth 业务场景，message 为精准中文文案

| 枚举常量 | code | message |
|---|---|---|
| EMAIL_ALREADY_REGISTERED | 409 | 邮箱已注册 |
| SEND_CODE_TOO_FREQUENT | 409 | 发送过于频繁，请60秒后再试 |
| INVALID_LOGIN_TYPE | 400 | 登录类型非法 |
| PASSWORD_EMPTY | 400 | 密码不能为空 |
| CODE_FORMAT_ERROR | 400 | 验证码格式不正确 |
| NOT_REFRESH_TOKEN | 400 | 不是有效的refreshToken |
| EMAIL_OR_PASSWORD_ERROR | 401 | 邮箱或密码错误（防枚举统一提示） |
| CODE_EXPIRED | 401 | 验证码已过期 |
| CODE_ERROR | 401 | 验证码错误 |
| REFRESH_TOKEN_INVALID | 401 | refreshToken已失效，请重新登录 |
| ACCOUNT_DISABLED | 401 | 账号已禁用，请重新登录 |

#### 20.2.2 BusinessException 构造器参数接口化
- `BusinessException(ErrorCodeEnum)` → `BusinessException(BaseExceptionInterface)`
- `BusinessException(ErrorCodeEnum, String)` → `BusinessException(BaseExceptionInterface, String)`
- 单 String message 构造器保留（兜底 code=INTERNAL_ERROR 500）

**效果**：任何模块自定义的错误枚举只要实现 `BaseExceptionInterface` 都能直接传入，不再强依赖 common 层具体枚举类。

### 20.3 AuthServiceImpl 13 处异常调用
全部从双参数写法改为单参数 `BusinessException(AuthErrorCode.XXX)`，不再重复 message。

### 20.4 编译验证
`mvn compile` BUILD SUCCESS（JDK 21 / 7.1s）

---

## 21. YML 多环境拆分 + schema.sql 编码修复 + 设计契约对齐

### 21.1 YML 多环境拆分（3 文件）

| 文件 | 用途 | git 状态 |
|---|---|---|
| `application.yml` | 公共配置（server/Jackson/MyBatis-Plus/JWT 过期时间/AI 提示词模板），无敏感信息 | ✅ 提交 |
| `application-dev.yml` | 开发环境真实凭据（MySQL 密码、QQ 邮箱 `3037749727@qq.com` + 授权码、MinIO 密钥、JWT secret） | ❌ gitignore 忽略 |
| `application-demo.yml` | 演示模板，所有凭据为 `<占位符>`，供他人参考复制 | ✅ 提交 |

`.gitignore` 追加规则：`application-dev.yml` / `application-prod.yml` / `application-local.yml`

### 21.2 schema.sql 编码损坏修复 + 设计契约对齐

**问题**：原 schema.sql 存在严重编码损坏（字段首字母丢失，如 `food` → `ood`、`record` → `ecord`、`body` → `ody`、`name` → `ame`、`total` → `otal` 等），且表结构与设计契约第 2.2 节 DDL 差异巨大。

**方案**：以 [设计契约.md](file:///d:/FitPulse/docs/设计契约.md#L49-L243) 的 DDL 为唯一基准，完全重写 schema.sql 13 张表。

**关键对齐点**：
- `user` 表：移除 nickname/avatar_url（归 user_profile）
- `user_profile`：补 nickname/avatar_url/bio，移除 activity_level/dark_mode
- `user_goal`：补 target_body_fat/weekly_workouts/daily_water_ml/start_date/target_date，移除 start_weight_kg/protein_g_per_kg
- `exercise`：用 is_system/user_id/image_url/description 替换 cover_url/video_url/steps/tips
- `workout_plan`：简化为 name/plan_type/estimated_min，移除 days_per_week/duration_days/is_template/difficulty
- `workout_plan_exercise`：用 sort_order 替换 day_no/order_no
- `workout_record`：用 record_date/duration_sec/total_volume/total_sets/total_reps 替换 start_time/end_time/duration_min
- `workout_set`：补 is_warmup/rpe，移除 is_pr/duration_sec
- `body_metric`：简化为 weight/body_fat/muscle/bmi/waist，移除 chest/hip/arm/thigh/calf/neck
- `food`：用 is_system/kcal_per_100g 替换 is_custom/serving_unit，移除 alias/barcode
- `meal_record`：用 quantity_g 替换 serving_g
- `water_log`：简化为 amount_ml，移除 drink_time
- `file_resource`：用 bucket/object_key/file_size/file_url 替换 object_name/size_bytes/file_md5/biz_type

---

## 22. Logback 启动报错修复（Empty or null pattern）

### 22.1 问题现象
FitnessApplication 启动即退出，报错：
```
Logging system failed to initialize using configuration from 'null'
ERROR in ch.qos.logback.classic.PatternLayout("null") - Empty or null pattern.
```
原因：logback 初始化在 Spring Boot 最早期（environmentPrepared），任何配置错误都会直接导致 JVM 退出。

### 22.2 定位
[logback-spring.xml](file:///d:/FitPulse/fitness-backend/src/main/resources/logback-spring.xml#L8) 第 8 行：
```xml
<pattern></pattern>   <!-- 空字符串，正好对应 Empty pattern -->
```
但文件顶部已定义 `LOG_PATTERN` property，未引用。

### 22.3 修复
将空 pattern 改为引用 `${LOG_PATTERN}`：
```xml
<pattern>${LOG_PATTERN}</pattern>
```

---

## 23. logback-spring.xml 按通用项目规范增强（适配 FitPulse）

### 23.1 核心改动（与原极简版本对比）
| 维度 | 原配置 | 新配置 |
|---|---|---|
| 变量 | 只有 pattern / path | 分 `CONSOLE_PATTERN`(含 `%clr` 彩色) / `FILE_PATTERN`(干净落地) / charset / cap 5项 |
| Appender 数 | 仅 CONSOLE（控制台） | 3 个：CONSOLE + **FILE(按日+大小滚动50MB/30天/总10GB gz)** + **ERROR_FILE(LevelFilter只收ERROR)** |
| 分包 logger | 无 | `auth=DEBUG`、`mapper=DEBUG`、`spring-security=INFO`、`jjwt=WARN`（additivity=false 避免重复打印） |
| 环境隔离 | 无 | `<springProfile name="dev">` 业务包=DEBUG；非 dev=INFO；两份独立 root 注册 |
| 热加载 | 无 | `scan="true" scanPeriod="30s"` 运行时可改配置不用重启 |
| 彩色 | 无 | 控制台 `%clr(%-5level)`，Spring Boot 原生支持 → **已回退**：`%clr` 为 Spring Boot 自定义 converter，在本环境初始化时出现 `There is no conversion class registered for composite conversion word [clr]` 导致启动直接失败；已改为纯 logback 原生 `%-5level`，其他结构完整保留（分 profile/分包 logger/ FILE+ERROR rolling 全不动） |

### 23.2 关键文件
- 配置：[logback-spring.xml](file:///d:/FitPulse/fitness-backend/src/main/resources/logback-spring.xml)
- 落地目录：项目根下 `./logs/`（fitpulse.log 当前；fitpulse.2026-08-18.0.log.gz 滚动归档；error.log 错误专档）

---

## 24. Spring Boot 启动阻塞项 2 项清理（AI 自动装配 + 默认密码告警）

### 24.1 问题 1：Spring AI OpenAiAutoConfiguration Bean 创建失败
```
BeanCreationException: openAiChatModel → Assert.hasText(apiKey) fail
message: OpenAI API key must be set.
```
AI 模块（设计契约 6.6 节）还没开发，但 pom 引入了 `spring-ai-openai-spring-boot-starter` → 自动装配无条件生效 → dev.yml 中 `api-key:` 空字符串直接触发 `IllegalArgumentException`。

### 24.2 问题 2：UserDetailsService 默认内存用户 + 控制台打印随机密码
```
Using generated security password: 876e3b30-c5ef-493b-89f2-2ee1a2a61a03
```
FitPulse 是无状态 JWT 鉴权（JwtAuthFilter + SecurityContextHolder 写入），完全不需要 Spring Security 的 FormLogin/UserDetailsService 默认体系，该自动配置纯属噪音。

### 24.3 修复（1 处改动，application.yml spring 块）
追加 `spring.autoconfigure.exclude` 列表：
```yaml
autoconfigure:
  exclude:
    # 1. 关闭默认密码生成（与JWT无状态鉴权冲突）
    - org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
    # 2. 关闭Spring AI自动装配（未开发前避免api-key空Assert）
    - org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration
```
> 后续开发 AI 模块时：从 exclude 列表移除 OpenAiAutoConfiguration 行，在 application-dev.yml 填真实 `spring.ai.openai.api-key` 即可。

---

## 25. 注册流程新增验证码（契约+代码+配置全链路）

### 25.1 需求背景
此前 `/auth/register` 接口仅校验 email+password 即可注册，与登录流程（必须带验证码）不对称，存在"机器人批量注册"风险。按用户要求，**注册也必须先走发码→再带 code 注册**，且注册验证码与登录验证码在 Redis key 前缀上完全隔离，避免混淆与互用。

### 25.2 契约更新（docs/设计契约.md 6.1 节）
- 原 `/auth/register` 请求体 `{email, password}` → **新增 `code` 字段**（6位数字注册验证码）
- **新增接口** `/auth/register/send-code`：POST，免鉴权，请求 `{email(@qq.com)}`，响应 null
- `/auth/login/send-code` 备注补充：与注册验证码前缀完全隔离（明确两个场景互不相通）

### 25.3 RedisKeyConstants（注册独立前缀）
- 新增常量 `REGISTER_CODE = "fitpulse:register:code:%s"`
- 新增静态方法 `buildRegisterCodeKey(String email)`
- 登录验证码仍走 `fitpulse:login:code:%s`，两者物理隔离

### 25.4 AuthErrorCode 新增 5 个注册场景专属枚举
| 枚举 | code | 场景 |
|---|---|---|
| REGISTER_SEND_CODE_TOO_FREQUENT | 409 | 注册验证码60秒防刷 |
| REGISTER_CODE_EMPTY | 400 | register请求中验证码为空 |
| REGISTER_CODE_FORMAT_ERROR | 400 | register请求中验证码非6位数字 |
| REGISTER_CODE_EXPIRED | 401 | Redis中注册验证码已过期/不存在 |
| REGISTER_CODE_ERROR | 401 | 用户输入注册验证码与Redis不一致 |
登录原有的 SEND_CODE_TOO_FREQUENT / CODE_EXPIRED / CODE_ERROR 文案改为精确标注"登录场景"，避免两套枚举语义混淆。

### 25.5 DTO 调整（2处）
- **RegisterReq**：新增字段 `code`，加 `@NotBlank` + `@Pattern(^\d{6}$)`（后端兜底校验，与Service层精确抛枚举双重保障）
- **新增 RegisterSendCodeReq**：`auth/dto/req/RegisterSendCodeReq.java`，email `@NotBlank @Email @Pattern(.+@qq\.com$)`

### 25.6 Service 接口与实现
- **AuthService**：新增 `void registerSendCode(RegisterSendCodeReq req)` 方法声明
- **AuthServiceImpl**：
  1. `registerSendCode` 实现：先查 user 表邮箱已注册则直接拒（EMAIL_ALREADY_REGISTERED）→ 60s 防刷（rate key 用 register 前缀）→ 生成6位码 → Redis 双写（code 5min + rate 60s）→ 日志输出 → 邮件发送主题为"FitPulse 注册验证码"
  2. `register` 改造：最**先**走新私有方法 `verifyRegisterCodeAndConsume(email, code)`（空/格式/过期/错误四分支分别对应 REGISTER_CODE_* 四枚举，成功后 delete Redis key 一次性消费）→ 然后才做邮箱唯一检查+写user
  3. 新增私有 `verifyRegisterCodeAndConsume`，与登录 `verifyCodeAndConsume` 完全隔离（key前缀/异常枚举均不同）

### 25.7 Controller + SecurityConfig 白名单
- **AuthController**：新增 `POST /register/send-code` 端点，`@Valid RegisterSendCodeReq` → `authService.registerSendCode(req)`
- **SecurityConfig WHITELIST**：追加 `/api/v1/auth/register/send-code` 到 permitAll（否则匿名请求会被 401 拦截）

### 25.8 最终接口清单（Auth模块 6 条）
| 方法 | 路径 | 鉴权 | 关键说明 |
|---|---|---|---|
| POST | /register/send-code | 否 | 发注册码，邮箱已注册直接拒，60s防刷 |
| POST | /register | 否 | 必须带正确注册码，成功即消费删除 |
| POST | /login/send-code | 否 | 发登录码，独立Redis前缀 |
| POST | /login | 否 | type=1密码 / type=2验证码 |
| POST | /refresh | 否 | refreshToken旋转 |
| POST | /logout | 是 | 删Redis refreshToken |

### 25.9 编译验证
`mvn -q -DskipTests compile` exit 0 ✅

---

## 26. 修复 JDBC URL `characterEncoding=utf8mb4` → HikariPool 初始化失败（UnsupportedEncodingException）

### 26.1 报错堆栈定位
```
java.sql.SQLException: Unsupported character encoding 'utf8mb4'
  at com.mysql.cj.jdbc.ConnectionImpl.<init>(ConnectionImpl.java:434)
  ...
Caused by: java.io.UnsupportedEncodingException: utf8mb4
  at java.base/java.lang.String.lookupCharset(String.java:861)
  at com.mysql.cj.util.StringUtils.getBytes(StringUtils.java:235)
```
触发路径：调用 `AuthServiceImpl.registerSendCode → UserMapper.selectCount` → MyBatis SpringManagedTransaction 开连接 → HikariPool `checkFailFast` 初始化 pool。

### 26.2 为什么错
- **`utf8mb4` 是 MySQL 服务端字符集名**（在 DDL / `SET NAMES utf8mb4` / `connectionCollation` 这样的 MySQL 参数里合法）
- **JDBC `characterEncoding=` 参数传的是 Java `Charset` 名**，Java 自带 charset 表里只有 `UTF-8`、`UTF8`、`US-ASCII` 等，没有 `utf8mb4`。
- 驱动最终走 `String.getBytes(name)` → `Charset.forName(name)` → `String.lookupCharset("utf8mb4")` 直接 `UnsupportedEncodingException`。

### 26.3 修复（演示模板 application-demo.yml）
```yaml
# 修复前：
url: jdbc:mysql://...fitpulse_db?useUnicode=true&characterEncoding=utf8mb4&...

# 修复后：
url: jdbc:mysql://...fitpulse_db?useUnicode=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_unicode_ci&...
```
- `characterEncoding=UTF-8`：Java 端传输字节使用 UTF-8（MySQL Connector/J 识别后会告诉 MySQL `SET NAMES utf8mb4`，因为 UTF-8 映射到 MySQL 的 utf8mb4，不用担心中文 emoji 存不下）
- `connectionCollation=utf8mb4_unicode_ci`：显式告诉 MySQL 连接排序规则对齐 DDL（schema.sql 表 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci），避免 fallback 到数据库默认 collation 不一致导致比较/唯一索引行为差异。

### 26.4 ⚠️ 你本地必须同步改（gitignore 中的 application-dev.yml）
演示模板在资源目录下，但你的启动报错来自**实际激活的 profile=dev**，即 `application-dev.yml`（被 `.gitignore` 忽略，仓库里看不到）。它里面的 JDBC URL 必然也写了 `characterEncoding=utf8mb4`，否则堆栈不会抛错。

请把本地 `application-dev.yml` 中 `spring.datasource.url` 这一行按 26.3 的修复改完，然后重启 FitnessApplication 即可。改完后 register/send-code 会走到 HikariPool 初始化成功 → 成功查询 user 表 + 写 Redis 验证码。

---

## 27. 修正"发送验证码接口响应不返回验证码明文" + apifox/md 文档同步

### 27.1 问题现象（Apifox 两张截图）
1. 调完 `/auth/register/send-code`（或 `/auth/login/send-code`）后想继续调 `/auth/register` / `/auth/login type=2`，但 Apifox 响应里**看不到验证码值**，只能切到 IDEA 控制台翻 `log.info("[注册验证码] code=xxx")`，联调成本高；
2. 用户把**响应外层的 `code: 0`（Result 业务状态码"成功"）** 误以为是"验证码值"，就以为"接口返回的 code=0 和控制台输出的 6 位数字不是同一个"；
3. 另外担心 `接口文档_apifox.json` 的 `/auth/register` 请求体缺少 `code` 字段、无法进行契约校验。

### 27.2 文档现场核对（`接口文档_apifox.json`）
用 Grep 定位后读取片段：
- `#/components/schemas/RegisterReq` 的 `required` 已含 `"email","password","code"`，`properties.code` 有 `pattern=^\\d{6}$`，`example` 含 `"code":"482917"` —— **json 契约实际是正确的**，Apifox 客户端"导入后没刷新/缓存旧版本"会显示成缺字段，需重新导入或手动 Reload OpenAPI schema。
- 两个 send-code 响应的 schema 只引用 `Result`，example `data: null` —— 这个才是真正让用户"看不到验证码"的根源。

### 27.3 代码改动
1. 新增 [SendCodeResp.java](file:///d:/FitPulse/fitness-backend/src/main/java/com/fitpulse/app/auth/dto/vo/SendCodeResp.java)（code:String 6位数字 + expireMinutes:Long + rateLimitSeconds:Long，均来自实现类常量）。
2. [AuthService.java](file:///d:/FitPulse/fitness-backend/src/main/java/com/fitpulse/app/auth/service/AuthService.java) 两个 send-code 返回值由 `void` → `SendCodeResp`。
3. [AuthServiceImpl.java](file:///d:/FitPulse/fitness-backend/src/main/java/com/fitpulse/app/auth/service/impl/AuthServiceImpl.java) 两个方法在发送邮件后 `return SendCodeResp.builder().code(code).expireMinutes(CODE_EXPIRE_MIN).rateLimitSeconds(CODE_RATE_LIMIT_SEC).build()` —— 保证 `SendCodeResp.code` **与日志输出、邮件正文、Redis 存储四值完全一致**。
4. [AuthController.java](file:///d:/FitPulse/fitness-backend/src/main/java/com/fitpulse/app/auth/controller/AuthController.java) 两个方法由 `Result<Void>` → `Result<SendCodeResp>`，直接 `return Result.success(authService.xxx(req))`。

### 27.4 文档改动
1. [接口文档_apifox.json](file:///d:/FitPulse/docs/%E6%8E%A5%E5%8F%A3%E6%96%87%E6%A1%A3_apifox.json)
   - schemas 新增 `SendCodeResp`（code/expireMinutes/rateLimitSeconds 三字段 + pattern/minimum 约束）
   - `/auth/register/send-code` 与 `/auth/login/send-code` 的 200 响应 schema 改为 `allOf(Result, {data: $ref SendCodeResp})`，example 分别用 `482917` / `952764` 示例
2. [接口文档.md](file:///d:/FitPulse/docs/%E6%8E%A5%E5%8F%A3%E6%96%87%E6%A1%A3.md) 2.1 / 2.4 两节
   - 原"响应 data=null"替换为"成功响应 data 字段"表格
   - 服务端行为补第 7/5 条"上述 data.code 明文回传=日志=邮件=Redis，方便联调"

### 27.5 响应体对照（调完 send-code 看到的真实结果）
```jsonc
// POST /auth/register/send-code  或  POST /auth/login/send-code
{
  "code": 0,          // 外层 Result 业务码：0=成功，不是验证码
  "message": "success",
  "data": {
    "code": "482917", // ← ✅ 这里才是 6 位验证码，和控制台 log.info 打印的完全一致
    "expireMinutes": 5,
    "rateLimitSeconds": 60
  },
  "timestamp": 1755410000000
}
```
用户只要在 Apifox 里把 send-code 的**后置操作**设置为"提取 `data.code` 到环境变量 REGISTER_CODE / LOGIN_CODE"，然后在 register/login 请求体中 `"code": "{{REGISTER_CODE}}"`，就能完成**自动化闭环**，不需要手填。

---

## 28. 最终确认：apifox.json 的 `/auth/register` 请求体"自动生成"为何只有 email+password（缺少 code）

### 28.1 现场复现（用户提供的 Apifox 截图）
Apifox 打开 `POST /api/v1/auth/register` → Body → JSON → 点"自动生成"，生成结果：
```json
{
  "email": "3037749727@qq.com",
  "password": "Fire@2026"
  // ⚠️ 没有 code 字段
}
```
用户据此怀疑"apifox.json 的注册接口没加 code 字段"。

### 28.2 真正原因（两层都要具备，缺一 Apifox 就可能漏）
Apifox / Postman / Swagger UI 在"按 schema 自动生成请求体"时，规则通常是：
1. **优先使用 `path.requestBody.content.*.example`（或 examples 里选中的那一份）**—— 这是每个接口**专属的 requestBody example**；
2. 只有当 path 下没有内联 example 时，才会 fallback 到 `components.schemas.Xxx.example`；
3. 有些版本的 Apifox 对"只写 `$ref`、没写内联 example"的路径在"自动生成"时**会走更保守的 fallback 逻辑**（可能只识别部分字段，或客户端缓存了旧 schema 导致结果不一致）。

上一轮改的只是 `components.schemas.RegisterReq.required/properties/example`，**没有在 `/auth/register` 这个 path 的 requestBody 里写内联 example**，所以才会出现"我看 schema 有 code，但 Apifox 自动生成没带 code"的跨层差异。用户的判断（"我看到的自动生成确实没带"）是正确的，我之前的判断（"schema 里有就够了"）**不严谨**。

### 28.3 修复（在 path 级别直接写内联 example，不依赖 $ref 解析）
1. **`/auth/register` 路径**（注册必填 code）：在 `requestBody.content.application/json` 里新增内联 `example`：
   ```json
   {"email":"fire_dev@qq.com","password":"Fire@2026","code":"482917"}
   ```
2. **`/auth/login` 路径**（type=1/2 两种模式）：用 OpenAPI 3.0 的 `examples`（带 key）而非 `example`，给出两份可切换模板：
   - `passwordLogin`：type=1，只带 password，不带 code
   - `codeLogin`：type=2，只带 code：`{"email":"fire_dev@qq.com","type":2,"code":"952764"}`
3. **JSON 语法校验通过**：`node -e "JSON.parse(...)"` ✅ JSON OK。

### 28.4 代码引用
- 修复片段位置：[接口文档_apifox.json §/auth/register requestBody.example](file:///d:/FitPulse/docs/%E6%8E%A5%E5%8F%A3%E6%96%87%E6%A1%A3_apifox.json#L2898-L2912) / [§/auth/login requestBody.examples](file:///d:/FitPulse/docs/%E6%8E%A5%E5%8F%A3%E6%96%87%E6%A1%A3_apifox.json#L2940-L2967)
- schema 层 RegisterReq 的 code 声明（保留双保险）：[接口文档_apifox.json#L1081-L1110](file:///d:/FitPulse/docs/%E6%8E%A5%E5%8F%A3%E6%96%87%E6%A1%A3_apifox.json#L1081-L1110)

### 28.5 用户侧操作（必做，否则仍然看到老结果）
> 不是"代码改完 Apifox 自动就刷新了"，Apifox 默认不会主动 watch 文件变更。
> 必须手动执行：**Apifox 项目主页 → 导入 → 选择 OpenAPI 3.0 (YAML/JSON) → 选 `docs/接口文档_apifox.json` → 覆盖导入 / 更新已有接口**。
> 导入完成后重新打开 `POST /auth/register`，Body → "自动生成"就一定是 email+password+code 三字段完整 JSON。

