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
    │   ├── books.ts         # 图书接口封装（listBooks / getBookById / addBook / updateBook / deleteBooks）
    │   ├── lends.ts         # 借阅记录接口封装（listLends / lendBook / backLend / deleteLends ...）
    │   ├── index.ts         # HttpClient 封装（baseURL http://localhost:8080, withCredentials）
    │   ├── handler.ts       # 响应处理（DataInfo 解包、错误码、401 处理）
    │   ├── map.d.ts         # 实体类型（readerInfo / adminInfo / bookInfo / lendInfo ...）
    │   └── response.d.ts    # 各接口响应类型（AuthLoginRes / BooksRes / AdminsRes / LendsRes ...）
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
        │   ├── Dashboard.vue    # 读者控制台：欢迎卡片 + 新书推荐 + 我的借阅
        │   ├── Profile.vue      # 我的信息（GET /api/readers/{id}）
        │   ├── BookBrowse.vue   # 图书浏览：搜索/筛选/详情/借阅入口
        │   └── BorrowedBooks.vue # 我的借阅（列表 / 借书 / 还书，/api/lends/**）
        └── admin/           # 管理员区域（AdminLayout 布局）
            ├── AdminLayout.vue
            ├── Dashboard.vue    # 管理控制台：欢迎卡片 + 统计卡片 + 快捷操作
            ├── BookInfo.vue     # 图书管理（增/删/改/查，/api/books/**）
            ├── Books.vue        # 借阅管理（列表/筛选/分页/借书/还书/批量删除，/api/lends/**）
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
| `/reader/dashboard` | `views/reader/Dashboard.vue` | 读者控制台：欢迎卡片 + 新书推荐 + 我的借阅 |
| `/reader/books` | `views/reader/BookBrowse.vue` | 图书浏览（搜索/筛选/详情/借阅入口） |
| `/reader/profile` | `views/reader/Profile.vue` | 我的信息 |
| `/reader/borrowed` | `views/reader/BorrowedBooks.vue` | 我的借阅（借书 / 还书 / 借阅历史） |
| `/admin/dashboard` | `views/admin/Dashboard.vue` | 管理控制台：欢迎卡片 + 统计 + 快捷操作 |
| `/admin/bookinfo` | `views/admin/BookInfo.vue` | 图书管理（增/删/改/查/筛选/分页） |
| `/admin/lends` | `views/admin/Books.vue` | 借阅管理（借书 / 还书 / 筛选 / 分页 / 批量删除） |
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
| GET | `/api/books` | `BooksRes` | 分页查询图书列表（`?pageNum&limit&name&typeId&status`） |
| POST | `/api/books` | `BookPostRes` | 新增图书（body 为 `bookInfo`） |
| DELETE | `/api/books` | `BookDeleteRes` | 批量删除图书（body 为 `number[]`） |
| GET | `/api/books/{id}` | `BookIdRes` | 按 ID 查询图书 |
| PUT | `/api/books/{id}` | `BookPutRes` | 更新图书（body 为 `bookInfo`） |
| GET | `/api/lends` | `LendsRes` | 分页查询借阅记录（`?pageNum&limit&bookName&readerName&backType`） |
| GET | `/api/lends/{id}` | `LendIdRes` | 按 ID 查询借阅记录 |
| GET | `/api/lends/reader/{readerId}` | `LendsPlainRes` | 读者借阅历史（不分页） |
| GET | `/api/lends/book/{bookId}` | `LendsPlainRes` | 图书借阅记录（不分页） |
| POST | `/api/lends` | `LendPostRes` | 借书 `{bookId, readerId}`（置图书 status=1） |
| PUT | `/api/lends/{id}/back` | `LendBackRes` | 还书 `{backType, exceptRemarks}`（置图书 status=0） |
| DELETE | `/api/lends` | `LendDeleteRes` | 批量删除借阅记录（body 为 `number[]`） |
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
- **图书管理（`/api/books`）**：
  - 管理端 `views/admin/BookInfo.vue` —— 分页列表 + 书名/分类/状态筛选 + 新增 + 编辑 + 批量删除
  - 读者端 `views/reader/BookBrowse.vue` —— 分页列表 + 书名/分类筛选 + 详情模态框 + 借阅入口
  - 封装层 `src/api/books.ts`（listBooks / getBookById / addBook / updateBook / deleteBooks）
- **控制台页面**：
  - 读者控制台 `views/reader/Dashboard.vue` —— 欢迎卡片（姓名/编号/借阅统计）+ 新书推荐（6 本可借图书卡片）+ 我的借阅（最近记录表格）
  - 管理控制台 `views/admin/Dashboard.vue` —— 欢迎卡片 + 统计（图书总数/可借/已借出/借阅记录）+ 快捷操作（图书/借阅/管理员/读者管理入口）
- 借阅管理（`/api/lends`）：
  - 读者端 `views/reader/BorrowedBooks.vue` —— 我的借阅列表（`/api/lends/reader/{id}`）、借书（`POST /api/lends`）、还书（`PUT /api/lends/{id}/back`）
  - 管理端 `views/admin/Books.vue` —— 分页列表 + 书名/读者姓名模糊筛选 + backType 状态筛选、借书、还书、批量删除（`DELETE /api/lends`）
  - 封装层 `src/api/lends.ts`（listLends / getLendById / listLendsByReader / listLendsByBook / lendBook / backLend / deleteLends）

### 待开发 / 改进
- 读者区域：ChangePassword（读者改密，后端暂无对应接口）
- 管理员区域：Admins 的增 / 删 / 改（后端接口已就绪，前端仅列表）
- 分页：`GET /api/admins` 的 `page&size` 参数前端尚未接入
- 借阅管理的「借书」当前以图书 ID + 读者 ID 手工输入，未做图书 / 读者选择器联动
- 读者图书浏览的「去借阅」跳转至借阅页，未实现图书 ID 自动填充

## 环境要求
- Node.js `^22.18.0 || >=24.12.0`（见 `package.json` `engines`）
- pnpm（建议 v9+）
