# FitPulse 前端开发日志（进行中）

> 会话主题：基于接口文档 auth 部分实现登录/注册界面，清理 auth 外无关前端代码
> 起始时间：2026-08-18
> 状态：进行中（会话结束后由用户确认，重命名为 `<会话概括>_<日期>.md`）

---

## 一、需求与上下文

- 来源：`docs/接口文档.md` auth 模块（2.1~2.6）
- 后端确认：
  - 成功码 `200`（非 0），`Result<T>` 结构 `{code,message,data,timestamp}`
  - 6 个 auth 接口：`/auth/register/send-code`、`/auth/register`、`/auth/login/send-code`、`/auth/login`、`/auth/refresh`、`/auth/logout`
  - 登录双模式：`type=1` 密码 / `type=2` 验证码
  - 发码响应 `data.code` **明文回传**验证码（开发联调用），含 `expireMinutes`、`rateLimitSeconds`
  - 邮箱约束：必须 `@qq.com` 结尾
  - 密码约束：8-64，至少含字母+数字
  - 验证码：6 位数字，5min 有效，60s 防刷
  - refreshToken 旋转：旧的立即失效
  - 登出需 Bearer accessToken

## 二、UI 设计决策（已与用户确认）

| 维度 | 选择 |
|---|---|
| 视觉风格 | 极简清新健康（白底/浅底 + 品牌色点缀 + 干净排版） |
| 布局结构 | 居中单卡，卡片内顶部 Tab 切换 登录/注册 |
| 登录方式切换 | 内嵌文字 Tab（密码登录 / 验证码登录） |
| 附加依赖 | 引入 Tailwind CSS |

**视觉落点**：
- 背景：浅色（白/极浅灰蓝渐变），非全屏紫渐变
- 卡片：白底、圆角、细边框、柔和阴影
- 品牌紫 `#7c5cff` + 青 `#22d3ee` 仅用于：Logo 标识、主按钮、Tab 下划指示器、输入框聚焦环、小面积点缀
- "Pulse" 心率脉动元素：Logo 内小心电图标 + 发码按钮 loading 时的心跳微动效
- 字体：系统无衬线，标题大号粗体，副文案浅灰

**组件策略**：
- 表单用原生 input + Tailwind 手写样式（比 Element Plus 默认样式更干净）
- 表单校验：自定义 computed 校验 + 内联错误文案
- `ElMessage`：仅用于后端错误/成功 toast
- `@element-plus/icons-vue`：输入框前后图标（Mail/Lock/Key/View/Hide）
- 倒计时按钮：手写 setInterval 逻辑

## 三、任务清单

1. ✅ 读取接口文档 + 后端 Result/AuthController 确认协调点
2. ✅ 与用户确认 UI 设计方向
3. ⏳ 引入 Tailwind CSS（安装 + 配置 tailwind.config + 引入指令）
4. ⏣ 清理 auth 外无关代码：删 `api/{dashboard,training,health,file,ai}.js`；精简 `style.scss`
5. ⏣ 重写 `api/auth.js`：仅保留 6 个 auth 接口（补全发码/刷新）
6. ⏣ 重写 `stores/user.js`：仅保留 login/logout/refreshToken，剔除 loadProfile
7. ⏣ 重写 `router/index.js`：仅 `/login`、`/register`、`/home`（极简登录后落地页）、`/` 重定向
8. ⏣ 新建 `views/auth/AuthPage.vue`：单卡 + 顶部 Tab（登录/注册）+ 登录内嵌 Tab（密码/验证码）
9. ⏣ 新建 `views/home/Home.vue`：极简登录后落地页（欢迎语 + 登出按钮，验证 token 链路）
10. ⏣ 调整 `request.js`：401 触发 refreshToken 自动续签 + 续签失败清凭证跳登录
11. ⏣ 本地启动 `npm run dev` 联调验证
12. ⏣ 会话结束 → 用户确认 → 重命名本日志为 `<会话概括>_<日期>.md`

---

## 四、开发记录

### 2026-08-18
- 完成接口文档与后端代码核对
- 完成与用户的 UI 设计方向确认
- 用户确认任务清单后进入编码

#### 执行完成（9/9）
1. ✅ 引入 Tailwind CSS：安装 tailwindcss@3 + postcss + autoprefixer；新增 [tailwind.config.js](file:///d:/FitPulse/fitness-web-admin/tailwind.config.js)（品牌色 pulse/pulse-cyan + heartbeat/fade-up/pulse-ring 动画）、[postcss.config.js](file:///d:/FitPulse/fitness-web-admin/postcss.config.js)、[src/styles/tailwind.css](file:///d:/FitPulse/fitness-web-admin/src/styles/tailwind.css)；main.js 引入顺序 tailwind.css → style.scss
2. ✅ 清理无关代码：删除 api/{dashboard,training,health,file,ai}.js；精简 [style.scss](file:///d:/FitPulse/fitness-web-admin/src/style.scss) 为仅 auth 链路样式（浅色渐变 .auth-wrap + 白卡 .auth-card）
3. ✅ 重写 [api/auth.js](file:///d:/FitPulse/fitness-web-admin/src/api/auth.js)：6 个接口 registerSendCode/register/loginSendCode/login/refreshToken/logout，路径 /api/v1/auth/*
4. ✅ 重写 [stores/user.js](file:///d:/FitPulse/fitness-web-admin/src/stores/user.js)：login(form 内部调 API)/refresh/logout/clearAuth，剔除 loadProfile 与 user 模块耦合
5. ✅ 重写 [router/index.js](file:///d:/FitPulse/fitness-web-admin/src/router/index.js)：/login /register /home + 重定向；已登录访问登录页自动跳 /home
6. ✅ 新建 [views/auth/AuthPage.vue](file:///d:/FitPulse/fitness-web-admin/src/views/auth/AuthPage.vue)：单卡 + 顶部Tab(登录/注册, 路由驱动) + 登录内嵌Tab(密码/验证码)；邮箱校验 @qq.com、密码 8-64 字母+数字、验证码 6 位；发码后 60s 倒计时 + 开发明文验证码 inline 提示；密码可见切换
7. ✅ 新建 [views/home/Home.vue](file:///d:/FitPulse/fitness-web-admin/src/views/home/Home.vue)：极简登录后落地页（欢迎语 + userId 展示 + 登出）
8. ✅ 重写 [utils/request.js](file:///d:/FitPulse/fitness-web-admin/src/utils/request.js)：401 自动 refreshToken 续签 + 队列重放 + refresh 接口自身 401 防递归 + 续签失败清凭证跳登录
9. ✅ Vite dev 启动成功 http://localhost:5173/ 无编译错误；浏览器验证 3 张截图（登录页/注册页/验证码登录模式）渲染均 PASS，Tab 切换正常，视觉风格符合"极简清新健康"

#### 待用户验证
- 启动后端（端口 8080）后实际联调：发码 → 收到明文验证码 → 注册/登录 → 跳 /home → 登出
- 401 续签链路（需 accessToken 过期场景）

#### 备注
- Element Plus dark css-vars 在控制台偶现 ERR_ABORTED，不影响渲染（el-icon / ElMessage 正常），暂不处理
- dev server 仍在运行（localhost:5173），便于用户继续联调

---

## 七、忘记密码前端实现（同步后端新增接口，fitness-web-admin）

### 背景
后端新增忘记密码双接口并已同步至 [接口文档.md](file:///d:/FitPulse/docs/接口文档.md) 2.6/2.7：
- `POST /auth/forgot-password/send-code` body `{email}` → `SendCodeResp{code,expireMinutes,rateLimitSeconds}`，邮箱未注册→404，60s 防刷→409，明文回传验证码
- `POST /auth/forgot-password/reset` body `{email,code,newPassword,confirmPassword}` → `data=null`，两次不一致→400，验证码校验，**不自动登录**跳登录页
- DTO 字段已核对：`ForgotPasswordSendCodeReq{email}` / `ForgotPasswordResetReq{email,code,newPassword,confirmPassword}`（与文档一致）

### UI 设计决策（已与用户确认）
| 维度 | 选择 |
|---|---|
| 入口位置 | 登录密码模式密码框下方右对齐 "忘记密码？" 链接 |
| 流程结构 | 独立路由 /forgot-password + 单卡单页表单（邮箱+发码/验证码/新密码/确认一次铺开） |
| 重置成功后 | ElMessage + router.replace('/login?email=xxx) 预填邮箱 |
| 视觉风格 | 复用既有"极简清新健康"（同 Logo/卡片/品牌色/输入框样式） |

### 执行完成（7/7）
1. ✅ [api/auth.js](file:///d:/FitPulse/fitness-web-admin/src/api/auth.js) 新增 `forgotPasswordSendCode(email)` + `forgotPasswordReset(data)`
2. ✅ [router/index.js](file:///d:/FitPulse/fitness-web-admin/src/router/index.js) 新增 `/forgot-password` 路由（免鉴权）
3. ✅ [AuthPage.vue](file:///d:/FitPulse/fitness-web-admin/src/views/auth/AuthPage.vue) 密码框下方右对齐 "忘记密码？" 链接 + `query.email` 预填（onMounted + watch）
4. ✅ 新建 [ForgotPassword.vue](file:///d:/FitPulse/fitness-web-admin/src/views/auth/ForgotPassword.vue)：单卡单页表单（邮箱+发码/倒计时/dev 明文、验证码、新密码+眼睛、确认密码+眼睛、重置按钮、返回登录）
5. ✅ 重置成功 → `router.replace({name:'Login', query:{email}})`
6. ✅ 浏览器验证 PASS：忘记密码页渲染齐全、入口链接右对齐、双向跳转、email query 预填均正常
7. ✅ 本日志追写 + git 提交

### 待用户验证
- 启动后端 8080 后联调：忘记密码发码（404 未注册场景）→ 重置 → 跳登录页预填邮箱 → 用新密码登录

---

## 五、移动端高保真原型开发（fitness-app-prototype）

> 会话主题：基于设计契约实现 FitPulse 移动端高保真交互原型（Vue 3 + Vite + Tailwind）
> 起始时间：2026-08-18
> 状态：原型骨架完成，待后续后端进度跟上后逐个完善对接

### 5.1 背景与决策
- 用户无安卓开发经验，经全景介绍 + 多方案对比（原生 Kotlin / Flutter / React Native / Capacitor+Vue / uni-app / PWA），确认采用 **Capacitor + Vue** 方案：复用 Vue 技术栈，浏览器调试 + 打包 APK
- 原型先于 Capacitor 接入，先产出高保真原型供用户审阅交互与视觉
- 视觉风格：明亮清新（白底 + 品牌紫 #7c5cff / 青 #22d3ee 点缀）
- 设计契约遵循：API 统一前缀 `/api/v1`、看板维度配色（A 紫 / B 蓝 / C 绿 / D 橙）、底部导航 4 项（训练/健康/AI/我的）

### 5.2 工程结构
```
fitness-app-prototype/
├── src/
│   ├── api/        (auth/dashboard/user/ai 接口封装)
│   ├── components/ (Layout 底部导航)
│   ├── mock/       (模拟数据)
│   ├── router/     (路由配置)
│   ├── stores/     (Pinia 用户状态)
│   ├── styles/     (Tailwind 入口)
│   ├── utils/      (request 请求封装)
│   └── views/      (Login/Home/Health/Ai/Profile 5 页)
├── package.json    (vue3/vite5/tailwind3/echarts5/pinia2)
├── vite.config.js  (端口 5174, base './')
└── tailwind.config.js (品牌色 + 维度色 + 动画)
```

### 5.3 任务清单与完成情况
1. ✅ 设计信息架构与冻结 Design Contract（路由 / 配色 / 维度高亮规则）
2. ✅ 创建工程骨架（package.json / vite.config / tailwind.config / postcss）
3. ✅ 实现共享层：main.js / App.vue / router / Layout（底部导航）/ mock / api / stores
4. ✅ 实现登录页 [Login.vue](file:///d:/FitPulse/fitness-app-prototype/src/views/Login.vue)
5. ✅ 实现训练看板页 [Home.vue](file:///d:/FitPulse/fitness-app-prototype/src/views/Home.vue)（B 蓝·本周训练容量 + C 绿·7 天容量趋势 ECharts 折线）
6. ✅ 实现健康看板页 [Health.vue](file:///d:/FitPulse/fitness-app-prototype/src/views/Health.vue)（A 紫·30 天体重趋势 + B 橙·7 天热量摄入柱状）
7. ✅ 实现 AI 顾问页 [Ai.vue](file:///d:/FitPulse/fitness-app-prototype/src/views/Ai.vue)（聊天气泡 + 建议按钮 + 思考指示器）
8. ✅ 实现个人中心页 [Profile.vue](file:///d:/FitPulse/fitness-app-prototype/src/views/Profile.vue)（用户信息 / 目标 / 基础数据 / 功能入口）
9. ✅ 静态自审 + 构建验证：`npm install` 115 包成功；`npm run build` 605 模块编译通过，产物输出 dist/（仅 ECharts chunk 大小警告，非错误）
10. ✅ Vite dev server 启动成功：http://localhost:5174/

### 5.4 待后续完善
- 后端 dashboard/training/health/ai/user 接口实现后，将 mock 切换为真实 API
- Capacitor 接入打包 APK
- 各页面交互细节打磨（根据用户预览反馈）

---

## 六、忘记密码功能同步（app-prototype）

> 会话主题：后端新增忘记密码接口，前端 app-prototype 同步实现（独立路由页方案）
> 起始时间：2026-08-18
> 状态：完成，已通过构建验证

### 6.1 后端新接口（已在后端 + 接口文档同步）
- `POST /api/v1/auth/forgot-password/send-code` body:`{email}` → data:`{code,expireMinutes,rateLimitSeconds}`（明文验证码）
  - 邮箱未注册 404（不防枚举）、60s 防刷 409、Redis key 前缀 `fitpulse:forgot-password:code:` 与注册/登录隔离
- `POST /api/v1/auth/forgot-password/reset` body:`{email,code,newPassword,confirmPassword}` → data:null
  - 两次密码不一致 400、验证码空/格式错 400、过期/错误 401；校验通过后一次性消费验证码（防重放）；不自动登录，前端跳登录页

### 6.2 前端实现决策（已与用户确认）
- 范围：仅 app-prototype（移动原型端），web-admin 暂不动
- UI 形式：独立路由页 `/forgot-password`（与 `/login` 同级，BottomNav 外），Login 登录密码模式底部加"忘记密码?"链接跳转

### 6.3 任务清单与完成情况
1. ✅ 扩展 [mock/index.js](file:///d:/FitPulse/fitness-app-prototype/src/mock/index.js)：新增 `mockForgotSendCode`（404/409/明文码，60s 防刷内存时间戳）、`mockForgotReset`（400/401/一次性消费验证码，防重放），严格对齐后端语义
2. ✅ 扩展 [api/auth.js](file:///d:/FitPulse/fitness-app-prototype/src/api/auth.js)：新增 `forgotPasswordSendCode(email)` / `forgotPasswordReset(data)`，走 `mockCall`
3. ✅ 新增路由 `/forgot-password` → [ForgotPassword.vue](file:///d:/FitPulse/fitness-app-prototype/src/views/ForgotPassword.vue)（[router/index.js](file:///d:/FitPulse/fitness-app-prototype/src/router/index.js)）
4. ✅ 新增 [ForgotPassword.vue](file:///d:/FitPulse/fitness-app-prototype/src/views/ForgotPassword.vue)：复用 Login 视觉风格；单页表单（邮箱 + 获取验证码60s倒计时 + 6位验证码 + 新密码8-64字母+数字 + 确认密码 + 重置按钮）；完整前端校验；开发明文验证码提示；新密码/确认密码可见切换；重置成功提示后跳 `/login`（不自动登录）
5. ✅ [Login.vue](file:///d:/FitPulse/fitness-app-prototype/src/views/Login.vue) 登录 Tab 密码模式底部加"忘记密码?" `router-link` + 样式
6. ✅ 静态自审 + `npm run build` 构建验证：607 模块编译通过，新增 `ForgotPassword` chunk（css 3.21kB / js 4.56kB），仅 ECharts chunk 大小警告（非错误）

### 6.4 关键设计点
- mock 用 `registeredEmails` 白名单（`fire_dev@qq.com`）模拟"已注册"，其他 QQ 邮箱返回 404，对齐后端"邮箱未注册"语义
- mock 用 `forgotCodeStore` Map 记录已发送验证码（code + sentAt），reset 时校验一致后立即 delete（一次性消费）
- 重置成功后 `setTimeout 1s` 跳转 `/login`，对齐后端"不自动登录、不返回 Token"
- 视觉与 Login.vue 1:1 对齐（品牌渐变 logo + 白卡 + 品牌色聚焦环 + 渐变主按钮），新增 success-tip（绿色成功提示）

### 6.5 待后续完善
- web-admin（PC 端）忘记密码功能（本次未做，后续按需补充）
- 接真实后端联调验证（当前走 mock）

---

## 八、项目架构文档同步（Android → Capacitor + Vue）

> 会话主题：移动端技术栈从 Android 原生（Kotlin+Compose+Hilt）切换为 Capacitor + Vue 工作流，同步更新项目架构与功能说明文档
> 起始时间：2026-08-18
> 状态：完成

### 8.1 背景
- 用户明确弃用 Android 原生开发方案，改用 Capacitor + Vue 工作流
- 项目记忆中已保留：Android 环境配置、Capacitor vs 原生对比并得到用户认可、fitness-app-prototype 高保真原型已落地（Vue3 + Vite + Tailwind）

### 8.2 修改范围（docs/项目架构与功能说明.md，共 10 处）
1. ✅ 1.1 节定位：Android 客户端 → 移动端 (Capacitor + Vue)，强调"一套代码双端复用"
2. ✅ 单体架构设计理念：Android 与 Web → 移动端 与 Web
3. ✅ 二节 Mermaid 架构图：`Android App\nKotlin+Compose+Hilt` → `Mobile App\nCapacitor+Vue3+Tailwind`
4. ✅ 3.1 节重写：新增 Capacitor 原生能力桥接说明（preferences/camera/haptics/status-bar/splash-screen）、fitness-mobile 项目结构、与 Web Admin 同栈复用性说明
5. ✅ 四节技术选型表：删除 11 项 Android 原生依赖（AGP/Kotlin/Compose/Hilt/Room/Vico 等），替换为 Vue3+Tailwind+Capacitor 6.x 全家桶 + Dexie.js 离线缓存
6. ✅ 5.1 节注册登录流程：DataStore/Pinia → Pinia + Capacitor Preferences；Android 跳转 → 移动端跳转；用户打开Web/Android → Web/移动端
7. ✅ 5.6 节 AI 对话流程：Web或Android → Web或移动端
8. ✅ 六节 Phase 1-5 里程碑：Android骨架/Android DataStore/Vico/AI Bubble/Room 离线 → 移动端 Vue 原型骨架/Capacitor Preferences/ECharts/@capacitor/camera/Dexie.js；Phase 5 改名为"移动端交互联调"
9. ✅ 7.4 节调试指南：Android Studio → Capacitor + Vue 三阶段（Web 开发期/原生壳联调/原生能力排查），含 `npx cap init/add/sync/open` 命令与 BASE_URL 说明
10. ✅ 遗漏修正：3 处残留的 "Android" 字样（非 Capacitor 上下文）统一替换为"移动端"

### 8.3 关键设计点
- 保留 Capacitor 上下文内的 "Android" 字眼（生成 Android 工程、打包 APK、Android Studio 打开原生壳），避免完全抹除导致使用者误解原生壳平台
- 技术选型表保留 ECharts，替代 Vico，与 Web Admin 组件复用形成正循环
- 调试指南明确"阶段一浏览器开发覆盖 90% 联调"，对齐 Capacitor 的实际开发节奏

### 8.4 后续
- fitness-mobile 工程独立初始化（当前复用 fitness-app-prototype 原型骨架）
- Capacitor Android 平台接入：`npx cap add android` 与 `capacitor.config.ts` 配置

---

## 九、全局规则追加（2026-08-18 用户明确）
1. **devlogs 临时日志规则**：每次会话开始 → 新建/复用 `devlogs/in_progress.md` 记录开发过程；会话结束且用户确认完成后 → 重命名为 `yyyyMMdd_<会话概括>.md`（本规则原为项目级约束，本次再次全局化确认）
2. **每次改动 git 提交规则**：每次完成文件改动（代码/文档/配置）后必须立即执行 git commit，保持提交粒度清晰（本规则追加为强制执行）
3. **任务讨论确认规则**：每次制定任务前必须先与用户讨论方案，用户明确确认后方可开始执行（避免方案偏差返工）

---

## 十、Controller 请求日志切面（@RequestLog 注解 + AOP）

> 会话主题：基于自定义注解 + Spring AOP 切面实现 Controller 请求日志打印（日期/方法/请求方式/参数等）
> 起始时间：2026-08-18
> 状态：完成，编译通过

### 10.1 需求与方案讨论（已与用户确认）
- **技术栈**：Spring AOP + 自定义注解，不使用 Servlet Filter / Interceptor（注解更精细，仅拦截必要方法）
- **依赖**：新增 `spring-boot-starter-aop`（同时引入 spring-aop 与 aspectjweaver）
- **生效方式**：方法级显式开关，仅标注 `@RequestLog` 的方法才记录日志，未标注方法无任何切面开销
- **日志输出位置**：仅控制台（用户明确，不入独立文件）
- **GET 请求参数**：额外解析 QueryString（用户明确要求），配合 @RequestParam/@PathVariable 一起采集
- **脱敏工具类位置**：`common/util/LogMaskUtil.java`（用户明确要求放在 util 下，而非 aspect 内部）
- **演示范围**：仅在 AuthController 的 3 个典型接口标注（login / logout / forgotPasswordSendCode）

### 10.2 文件清单
| 文件 | 类型 | 说明 |
|---|---|---|
| `fitness-backend/pom.xml` | 修改 | 新增 spring-boot-starter-aop 依赖 |
| `common/annotation/RequestLog.java` | 新建 | 注解定义，方法级，含 value/logArgs/logResult/logCost/maskFields 5 个属性 |
| `common/util/LogMaskUtil.java` | 新建 | 脱敏工具类，默认脱敏 password/token/code 等 10 个字段，支持自定义扩展 |
| `common/aspect/RequestLogAspect.java` | 新建 | 切面实现，@Around 环绕通知 |
| `auth/controller/AuthController.java` | 修改 | 3 个接口添加 @RequestLog 注解 |

### 10.3 注解设计
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestLog {
    String value() default "";          // 接口描述
    boolean logArgs() default true;      // 是否打印入参
    boolean logResult() default true;   // 是否打印返回值
    boolean logCost() default true;      // 是否打印耗时
    String[] maskFields() default {};   // 额外脱敏字段
}
```

### 10.4 日志格式
```
========== [RequestLog] 用户登录 ==========
Trace-ID  : 9f3a2b1c8d
Timestamp : 2026-08-18 22:30:15.456
URI       : /api/v1/auth/login
Method    : POST
Controller: AuthController#login
IP        : 192.168.1.10
UA        : Mozilla/5.0 (Windows NT 10.0)
Params    : {"username":"fire_dev","password":"******","type":1}
---------- 执行中 ----------
Result    : {"code":200,"message":"操作成功","data":{...}}
Cost      : 56ms
Status    : SUCCESS
========== 请求结束 ==========
```
异常路径打印 `Status: FAILED` + `Exception` + `Message`，原异常透传不影响 GlobalExceptionHandler。

### 10.5 关键设计点
1. **Trace-ID**：UUID 前 12 位，便于串联同一请求的多条日志
2. **参数采集**：
   - POST/PUT/DELETE：解析 @RequestBody 整体 + @RequestParam + @PathVariable
   - GET：在上述基础上额外解析 `request.getQueryString()`，URL 解码后合并到参数 Map（@RequestParam 已解析的优先保留）
3. **脱敏**：
   - 默认字段：password、passwordHash、oldPassword、newPassword、confirmPassword、secret、token、accessToken、refreshToken、code、captcha
   - 规则：字符串值 → `******`；非字符串值（数字、布尔）保留原值避免类型歧义
   - 嵌套对象递归处理
   - 脱敏过程异常不抛出，原样返回（保证日志打印不被脱敏逻辑阻塞）
4. **IP 解析**：依次尝试 X-Forwarded-For → X-Real-IP → Proxy-Client-IP → WL-Proxy-Client-IP → remoteAddr，多级代理取第一个
5. **不可序列化参数跳过**：HttpServletRequest/Response、HttpSession、MultipartFile 不参与 JSON 序列化
6. **异常透传**：`@Around` 捕获异常 → 记录 FAILED 日志 → 重新 throw，不干扰 `GlobalExceptionHandler` 的统一异常处理

### 10.6 编译验证
- 首次编译失败：`Method.getDeclingClass()` 拼写错误（应为 `getDeclaringClass()`）
- 修正后 `mvn compile` 通过，无 warning（同步清理未使用的 import：`JSONUtil`、`ArrayList`、`List`）

### 10.7 后续
- 待用户启动后端联调验证：调用 `/auth/login` 观察控制台日志格式与脱敏效果
- 其他 Controller（Dashboard/Training/Health/User/Ai）后续按需添加 @RequestLog 注解
- 可选优化：未来若需独立 request.log 文件，可在 logback-spring.xml 中添加独立 appender + logger



