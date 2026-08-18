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

