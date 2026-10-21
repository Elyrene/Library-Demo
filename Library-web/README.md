# 图书馆管理系统 — 前端（Library-web）

基于 **Vue 3 + TypeScript + Vite** 的图书馆管理系统前端工程，对接 Spring Boot 3 后端（接口定义见仓库根目录 [api-spec.json](api-spec.json)）。

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue（组合式 API + `<script setup>`） | ^3.5.42 |
| 语言 | TypeScript | ~6.0.0 |
| 构建 | Vite | ^8.2.2 |
| 路由 | Vue Router（`createWebHistory`） | ^5.3.1 |
| 状态管理 | Pinia（`pinia-plugin-persist` 持久化） | ^4.0.3 |
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
    ├── main.ts              # 应用入口：注册 Pinia（persist 插件）+ Router
    ├── App.vue              # 根组件，仅 <router-view>
    ├── assets/              # 全局样式 base.css / main.css、logo.svg
    ├── api/
    │   ├── index.ts         # HttpClient 封装（baseURL http://localhost:8080, withCredentials）
    │   ├── handler.ts       # 响应处理（DataInfo 解包、错误码、401 处理）
    │   ├── map.d.ts         # 实体类型映射（Admin / ReaderInfo / LoginRequest ...）
    │   └── response.d.ts    # 统一响应类型 DataInfo<T>
    ├── router/index.ts      # 路由：/login、/regist、/
    └── stores/
        └── user.ts          # useUserStore：登录态、角色、cookie（persist）
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
| `/` | `src/views/Home.vue` | 首页：展示当前登录用户，提供「退出登录」 |
| `/login` | `src/views/Login.vue` | 登录页：用户名 / 密码 / 角色（admin / reader） |
| `/regist` | `src/views/Regist.vue` | 读者注册页：完整读者信息表单 |

## 认证流程

1. 用户在 `/login` 提交 `{username, password, role}`，前端调用 `POST /api/auth/login`。
2. 后端登录成功时把用户对象与角色写入 `HttpSession`，前端把返回的 `cookie` 存入 `localStorage`（`useUserStore` 持久化）。
3. 后续所有请求由 `HttpClient` 自动携带 `cookie`（`withCredentials: true`），`/api/admins/**` 与 `/api/readers/**` 未登录时返回 `401`。
4. 「退出登录」调用 `POST /api/auth/logout`，清空本地用户态并跳回 `/login`。

## 后端接口概览

> 完整定义见 [api-spec.json](api-spec.json)。所有接口统一返回 `DataInfo<T>`：`code`（`0` 成功 / `-1` 业务失败 / `401` 未登录）、`mes`、`count`（仅分页）、`data`。

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 登录 `{username, password, role}` |
| POST | `/api/auth/logout` | 登出 |
| GET | `/api/auth/me` | 当前登录用户（含 role 与 user） |
| GET | `/api/admins` | 分页查询管理员列表（`?page&size`） |
| POST | `/api/admins` | 新增管理员 `{username, password, adminType}` |
| DELETE | `/api/admins` | 批量删除管理员（body 为 `number[]`） |
| GET | `/api/admins/{id}` | 按 ID 查询管理员 |
| PUT | `/api/admins/{id}/password` | 修改管理员密码 `{password}` |
| GET | `/api/readers/{id}` | 按 ID 查询读者 |
| POST | `/api/readers/register` | 读者注册 |

角色（`role`）取值：`admin` / `reader`；管理员类型 `adminType`：`0` 普通 / `1` 超级管理员。

## 环境要求

- Node.js `^22.18.0 || >=24.12.0`（见 `package.json` `engines`）
- pnpm（建议 v9+）
