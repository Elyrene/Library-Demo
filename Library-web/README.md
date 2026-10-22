# 图书馆管理系统 — 前端（Library-web）

基于 **Vue 3 + TypeScript + Vite** 的图书馆管理系统前端工程，对接 Spring Boot 3 后端（接口定义见仓库根目录 [api-spec.json](api-spec.json)）。

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue（组合式 API + `<script setup>`） | ^3.5.42 |
| 语言 | TypeScript | ~6.0.0 |
| 构建 | Vite | ^8.2.2 |
| 路由 | Vue Router（`createWebHistory`） | ^5.3.1 |
| 状态管理 | Pinia（手写 localStorage/sessionStorage 持久化） | ^4.0.3 |
| 类型检查 | vue-tsc | ^3.3.11 |

> 未使用 UI 组件库，页面均为自实现的原生 Vue 组件。

## 目录结构

```
Library-web/
├── api-spec.json            # 后端 API 接口文档（OpenAPI 3.0）
├── index.html               # 入口 HTML
├── package.json
├── vite.config.ts           # Vite 配置（@ -> src 别名）
├── tsconfig*.json           # TypeScript 配置
└── src/
    ├── main.ts              # 应用入口：注册 Pinia + Router
    ├── App.vue              # 根组件，仅 <router-view>
    ├── assets/              # 全局样式 base.css / main.css、logo.svg
    ├── api/
    │   ├── index.ts         # HttpClient 封装（baseURL http://localhost:8080, withCredentials）
    │   ├── handler.ts       # 响应处理（DataInfo 解包、错误码、401 处理）
    │   ├── map.d.ts         # 实体类型（readerInfo / adminInfo / SessionData ...）
    │   └── response.d.ts    # 各接口响应类型（AuthLoginRes / AdminsRes ...）
    ├── router/index.ts      # 路由表 + 全局 beforeEach 守卫（登录态/角色校验）
    ├── composables/
    │   └── useLogout.ts     # 登出逻辑（调用后端 + 清本地态 + 跳登录）
    ├── stores/
    │   └── user.ts          # useUserStore：登录态/角色/cookie + 记住我双存储持久化
    └── views/
        ├── Home.vue         # 角色分流页：按角色跳 reader/admin 区域
        ├── Login.vue        # 登录页：角色选择 / 记住我 / 注册入口
        ├── Regist.vue       # 读者注册页
        ├── reader/          # 读者区域（ReaderLayout 布局）
        │   ├── ReaderLayout.vue
        │   ├── Dashboard.vue    # 占位
        │   ├── Profile.vue      # 我的信息（GET /api/readers/{id}）
        │   └── BorrowedBooks.vue # 占位
        └── admin/           # 管理员区域（AdminLayout 布局）
            ├── AdminLayout.vue
            ├── Dashboard.vue    # 占位
            ├── Books.vue        # 占位
            ├── Admins.vue       # 管理员列表（GET /api/admins）
            ├── Readers.vue      # 读者查询（GET /api/readers/{id}）
            └── ChangePassword.vue # 修改密码（PUT /api/admins/{id}/password）
```

## 项目部署

```sh
pnpm install
```

## 开发启动

```sh
pnpm dev
```

Vite 默认启动于 `http://localhost:5173`，并开启 `vite-plugin-vue-devtools` 热更新。

## 构建

```sh
pnpm build        # 先 type-check 再 build-only
pnpm type-check   # 仅执行 vue-tsc 类型检查
pnpm build-only   # 仅构建，不做类型检查
pnpm preview      # 本地预览构建产物
```

## 环境变量 / 代理

- 后端地址硬编码在 `src/api/index.ts`：`baseURL: 'http://localhost:8080'`，请求携带 `withCredentials: true`（依赖后端 Session/Cookie 认证）。
- 如需修改后端端口或路径，直接编辑 `src/api/index.ts` 中的 `HttpClient` 构造参数。

## 路由与页面

| 路由 | 视图 | 说明 |
|------|------|------|
| `/` | → `/home` | 根路径重定向 |
| `/login` | `views/Login.vue` | 登录页（公开） |
| `/regist` | `views/Regist.vue` | 读者注册（公开） |
| `/home` | `views/Home.vue` | 角色分流页：admin → `/admin/dashboard`，reader → `/reader/dashboard` |
| `/reader/dashboard` | `views/reader/Dashboard.vue` | 读者控制台（占位） |
| `/reader/profile` | `views/reader/Profile.vue` | 我的信息 |
| `/reader/borrowed` | `views/reader/BorrowedBooks.vue` | 借阅记录（占位） |
| `/admin/dashboard` | `views/admin/Dashboard.vue` | 管理控制台（占位） |
| `/admin/books` | `views/admin/Books.vue` | 图书管理（占位） |
| `/admin/admins` | `views/admin/Admins.vue` | 管理员列表 |
| `/admin/readers` | `views/admin/Readers.vue` | 读者查询 |
| `/admin/password` | `views/admin/ChangePassword.vue` | 修改密码 |

### 全局路由守卫（`router/index.ts` `beforeEach`）

1. **已登录访问公开页**（`/login`、`/regist`）→ 跳 `/home`。
2. **公开页**直接放行。
3. **未登录访问受保护路由** → 先尝试 `GET /api/auth/me` 用后端 Session 恢复登录态；失败则 `clearUser()` 并跳 `/login`。
4. **角色不匹配**（`meta.role` 与 `userStore.role` 不一致，如 reader 访问 `/admin/**`）→ 跳 `/home`。
5. 其余放行。

## 认证流程

1. 用户在 `/login` 提交 `{username, password, role}`，前端调用 `POST /api/auth/login`。
2. 后端登录成功时把用户对象与角色写入 `HttpSession`，前端调 `GET /api/auth/me` 取回完整用户，存入 `useUserStore`（按「记住我」标志决定落盘到 `localStorage` 或 `sessionStorage`，key 为 `library_key`）。
3. 后续所有请求由 `HttpClient` 自动携带 `cookie`（`withCredentials: true`），`/api/admins/**` 与 `/api/readers/**` 未登录时返回 `401`。
4. 刷新/重开页面时，全局守卫通过 `GET /api/auth/me` 用后端 Session 恢复登录态（前端存储仅作兜底）。
5. 「退出登录」调用 `POST /api/auth/logout`，清空本地用户态（含两处存储）并跳回 `/login`。

## 记住我

登录页「记住我」复选框控制会话持久化的存储介质：

| 勾选 | 存储位置 | 关闭浏览器后 |
|------|----------|--------------|
| ✅ 记住我 | `localStorage['library_key']` | 仍是登录态 |
| ❌ 不勾选 | `sessionStorage['library_key']` | 失效 |

实现位于 `src/stores/user.ts`：`rememberMe` 标志 + `setRememberMe()` 方法；`persist()` 按标志路由到对应存储并清理另一处；`initFromlocal()` 优先读 `localStorage`、降级 `sessionStorage`；`clearUser()` 两处同时清理。

> 注意：前端 `sessionStorage` 关闭即清，但后端 `HttpSession` 不感知——只要后端 Session 未过期且浏览器仍持有 cookie，理论上仍可访问受保护接口。要彻底失效需后端配合（如 `logout` 时 `session.invalidate()`）。

## 后端接口概览

> 完整定义见 [api-spec.json](api-spec.json)。所有接口统一返回 `DataInfo<T>`：`code`（`0` 成功 / `-1` 业务失败 / `401` 未登录）、`mes`、`count`（仅分页）、`data`。

| 方法 | 路径 | 前端响应类型 | 说明 |
|------|------|--------------|------|
| POST | `/api/auth/login` | `AuthLoginRes` | 登录 `{username, password, role}` |
| POST | `/api/auth/logout` | `AuthLogoutRes` | 登出 |
| GET | `/api/auth/me` | `AuthMeRes` | 当前登录用户（含 role 与 user） |
| GET | `/api/admins` | `AdminsRes` | 分页查询管理员列表（`?page&size`） |
| POST | `/api/admins` | `AdminsRes` | 新增管理员 `{username, password, adminType}` |
| DELETE | `/api/admins` | `AdminsRes` | 批量删除管理员（body 为 `number[]`） |
| GET | `/api/admins/{id}` | `AdminsRes` | 按 ID 查询管理员 |
| PUT | `/api/admins/{id}/password` | `AdminsPasswordRes` | 修改管理员密码 `{password}` |
| GET | `/api/readers/{id}` | `ReadersIdRes` | 按 ID 查询读者 |
| POST | `/api/readers/register` | `ReadersRegisterRes` | 读者注册 |

角色（`role`）取值：`admin` / `reader`；管理员类型 `adminType`：`0` 普通 / `1` 超级管理员（整数）。

## 项目进度

### 已完成
- 登录 / 注册 / 登出全流程（Session/Cookie + 记住我双存储）
- 全局路由守卫（登录拦截 + 角色隔离 + `/api/auth/me` 会话恢复）
- 角色分流首页（`/home`）
- 读者区域：我的信息（Profile，实时拉取后端数据）
- 管理员区域：管理员列表（Admins）、读者查询（Readers）、修改密码（ChangePassword）
- 统一 `HttpClient` 封装 + `DataInfo<T>` 响应解包 + 401 拦截
- 全部 TypeScript 类型定义（实体 + 各接口响应），`vue-tsc --noEmit` 通过

### 占位 / 待开发
- 读者区域：Dashboard、BorrowedBooks（借阅记录）
- 管理员区域：Dashboard（控制台）、Books（图书管理）
- 读者区域：ChangePassword（读者改密，后端接口见 api-spec）
- 管理员区域：Admins 的增 / 删 / 改（后端接口已就绪，前端仅列表）
- 分页：`GET /api/admins` 的 `page&size` 参数前端尚未接入

## 环境要求

- Node.js `^22.18.0 || >=24.12.0`（见 `package.json` `engines`）
- pnpm（建议 v9+）
