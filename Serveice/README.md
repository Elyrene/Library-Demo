# 图书馆管理系统 — 后端服务（Serveice）

基于 **Spring Boot 3 + MyBatis + MySQL** 的图书馆管理系统后端，提供管理员与读者两种角色的认证、管理员 CRUD、读者注册与查询、图书借阅与归还能力。

---

## 目录

- [技术栈](#一技术栈)
- [项目结构](#二项目结构)
- [核心机制](#三核心机制)
  - [统一响应体](#31-统一响应体-datainfo)
  - [认证方式](#32-认证方式session-cookie)
  - [登录拦截器](#33-登录拦截器)
  - [分页机制](#34-分页机制pagehelper)
  - [全局异常处理](#35-全局异常处理)
  - [读者编号生成](#36-读者编号生成)
- [接口列表](#四接口列表)
- [已知缺陷](#五已知缺陷)
- [启动方式](#六启动方式)
- [初始数据](#七初始数据)
- [项目文档](#八项目文档)

---

## 一、技术栈

| 组件 | 版本 / 说明 |
|------|------------|
| Java | 17 |
| Spring Boot | 3.5（web / validation / test） |
| MyBatis | mybatis-spring-boot-starter 3.0.3 |
| MySQL | 8.0，驱动 `com.mysql.cj.jdbc.Driver` |
| PageHelper | pagehelper-spring-boot-starter 2.1.1 |
| Lombok | `@Data` 等 |
| 构建 | Maven |
| 数据库 | MySQL 8（`library_db`，UTF8MB4） |

---

## 二、项目结构

```
src/main/java/com/Library/
├── app/
│   └── ServeiceApplication.java            # 启动类（@MapperScan("com.Library.mapper")）
├── config/
│   ├── WebConfig.java                      # CORS 配置 + 注册登录拦截器
│   └── AuthInterceptor.java                # 登录校验拦截器（未登录返回 401）
├── controller/
│   ├── AuthController.java                 # /api/auth    登录 / 登出 / 当前用户
│   ├── AdminController.java                # /api/admins  管理员 CRUD
│   ├── ReaderController.java               # /api/readers 读者注册 / 查询
│   ├── LendController.java                 # /api/lends   借阅 / 归还 / 查询 / 删除
│   ├── BookController.java                 # /api/books   图书 CRUD / 分页查询 / 批量删除
│   └── NoticeController.java               # /api/notices 公告 CRUD / 分页查询 / 批量删除（写操作仅管理员）
├── service/
│   ├── LendService.java                    # 借阅服务接口
│   ├── NoticeService.java                  # 公告服务接口
│   └── impl/
│       ├── LendServiceImpl.java            # 借阅服务实现（借书 / 还书事务）
│       └── NoticeServiceImpl.java          # 公告服务实现
├── mapper/
│   ├── AdminMapper.java                    # 管理员 Mapper（6 个方法）
│   ├── ReaderInfoMapper.java               # 读者 Mapper（4 个方法）
│   ├── BookInfoMapper.java                 # 图书 Mapper（findById / queryAll / insert / update / updateStatus / deleteByIds）
│   ├── LendListMapper.java                 # 借阅记录 Mapper（queryAll / insert / updateBack / 等）
│   └── NoticeMapper.java                   # 公告 Mapper（findById / queryAll / insert / update / deleteByIds）
├── entity/
│   ├── Admin.java                          # 管理员实体
│   ├── ReaderInfo.java                     # 读者实体
│   ├── BookInfo.java                       # 图书实体（status 0未借出 / 1已借出）
│   ├── LendList.java                       # 借阅记录实体（含 JOIN 展示字段）
│   ├── Notice.java                         # 公告实体
│   └── TypeInfo.java                       # 图书分类实体
├── controller/dto/
│   ├── LoginRequest.java                   # 登录 DTO
│   ├── RegisterRequest.java                # 注册 DTO
│   └── LendRequest.java                    # 借阅请求 DTO（bookId, readerId）
└── common/
    ├── DataInfo.java                       # 统一响应体
    ├── Contants.java                       # Session key 常量
    └── GlobalExceptionHandler.java         # 全局异常处理

src/main/resources/
├── application.yml                         # 数据源 / 端口 / MyBatis / PageHelper
└── mapper/
    ├── Library.sql                         # 建库建表 + 种子数据脚本
    ├── AdminMapper.xml                     # 管理员 SQL
    ├── ReaderInfoMapper.xml                # 读者 SQL
    ├── BookInfoMapper.xml                  # 图书 SQL
    └── LendListMapper.xml                  # 借阅记录 SQL（JOIN book_info / reader_info）
```
```

> 注意：实际包根为 `com.Library`（大写 L），启动类在 `com.Library.app` 下；Mapper 接口位于 `com.Library.mapper`，实体位于 `com.Library.entity`，公共类位于 `com.Library.common`，配置位于 `com.Library.config`。

---

## 三、核心机制

### 3.1 统一响应体 `DataInfo<T>`

```json
{
  "code": 0,          // 0 = 成功，-1 = 业务失败，401 = 未登录
  "mes": "ok",
  "count": 10,        // 分页总数（仅分页接口）
  "data": { ... }
}
```

- 注解 `@JsonInclude(NON_NULL)`：`null` 字段不序列化。
- 静态工厂：`ok()` / `ok(data)` / `ok(data, count)` / `fail(mes)` / `fail(code, msg)`。

### 3.2 认证方式：Session（Cookie）

- 登录成功后，将用户对象 + 角色写入 `HttpSession`（key 分别为 `user`、`type`，见 `Contants`）。
- 后续请求需携带 Cookie（前端 `withCredentials: true`）。
- 角色值：`"admin"` 或 `"reader"`，由登录时 `LoginRequest.role` 决定。

### 3.3 登录拦截器

`AuthInterceptor`（注册于 `WebConfig.addInterceptors`）：

- **保护范围**：`/api/admins/**`、`/api/readers/**`、`/api/books/**`、`/api/notices/**`
- **放行范围**：`/api/auth/**`（登录、登出、`/me`）、`OPTIONS` 预检请求
- **校验逻辑**：Session 中无 `user` 时，直接返回 **HTTP 401** + `{"code":401,"mes":"未登录或登录已过期"}`，无需各 Controller 重复判断
- **已登录用户**正常通过，由 Controller 继续业务逻辑
- **角色权限**：拦截器目前仅校验「是否登录」，未区分角色；`/api/notices` 的写操作（POST / PUT / DELETE）在 `NoticeController` 中通过 Session 的 `SESSION_TYPE` 比对，**仅管理员**可执行，非管理员返回 `code -1` + `"仅管理员可..."`（详见 [api-spec.json](./api-spec.json)）。`/api/admins` 的细粒度角色限制仍是已知缺陷 #7

### 3.4 分页机制（PageHelper）

```java
PageHelper.startPage(pageNum, limit);
List<Admin> list = adminMapper.queryAll(username, adminType);
long total = new PageInfo<>(list).getTotal();
```

- `pageNum` 从 **1** 开始，`limit` 为每页条数。
- 返回 `DataInfo.ok(list, total)`，前端据此计算总页数。

### 3.5 全局异常处理

`GlobalExceptionHandler` 捕获所有未处理异常，统一返回：

```json
{ "code": -1, "mes": "服务器内部错误，请稍后重试" }
```

### 3.6 读者编号生成

`ReaderController.generateReaderNumber()`：

- 前缀：`8120116`
- 后缀：从 `SELECT MAX(readerNumber) FROM reader_info` 取最大值，流水号 +1 后格式化为 **3 位**（`%03d`）。
- 表为空时从 `001` 开始。
- ⚠️ 当序号超过 999 时会溢出为 4 位（`1000`），后续递增正常。

---

## 四、接口列表

### 认证 `/api/auth`（无需登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/login` | 登录（按 `role` 区分管理员 / 读者） |
| POST | `/logout` | 登出（清除 Session） |
| GET | `/me` | 获取当前登录用户 |

### 管理员 `/api/admins`（需登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `` | 分页查询（支持 `username` 模糊搜索、`adminType` 筛选） |
| GET | `/{id}` | 按 ID 查询 |
| POST | `` | 新增（`adminType` 默认 1；用户名重复时返回"用户名已存在"） |
| PUT | `/{id}/password` | 修改密码（body: `{"password": "..."}`） |
| DELETE | `` | 批量删除（body: `List<Integer>` ids） |

### 读者 `/api/readers`（需登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/{id}` | 按 ID 查询 |
| POST | `/register` | 注册（自动生成读者编号） |

### 图书 `/api/books`（需登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `` | 分页查询（支持 `name` 模糊搜索、`typeId` / `status` 筛选） |
| GET | `/{id}` | 按 ID 查询 |
| POST | `` | 新增（`status` 默认 0；名称为空时拒绝） |
| PUT | `/{id}` | 修改图书信息（全字段） |
| DELETE | `` | 批量删除（body: `List<Integer>` ids） |
### 借阅 `/api/lends`（需登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `` | 分页查询（支持 `bookName` / `readerName` 模糊搜索、`backType` 筛选） |
| GET | `/{id}` | 按 ID 查询借阅详情 |
| POST | `` | 借书（body: `{"bookId":1,"readerId":1}`，图书已借出时拒绝） |
| PUT | `/{id}/back` | 还书（body: `{"backType":1,"exceptRemarks":""}`，已归还时拒绝） |
| GET | `/reader/{readerId}` | 读者借阅历史 |
| GET | `/book/{bookId}` | 图书借阅记录 |
| DELETE | `` | 批量删除（body: `List<Integer>` ids） |

### 公告 `/api/notices`（需登录）

> 读操作（GET）对所有登录用户开放；写操作（POST / PUT / DELETE）**仅管理员**可执行，非管理员返回 `code -1` + "仅管理员可..."。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `` | 分页查询（按 `createDate` 倒序，支持 `topic` / `content` 模糊搜索） |
| GET | `/{id}` | 按 ID 查询公告 |
| POST | `` | 发布公告（**仅管理员**，body: `{"topic":"...","content":"..."}`，`author` 默认 `admin`） |
| PUT | `/{id}` | 修改公告主题 / 内容（**仅管理员**，body: `{"topic":"...","content":"..."}`） |
| DELETE | `` | 批量删除（**仅管理员**，body: `List<Integer>` ids） |

> 完整参数 / 请求体 / 响应示例见 [api-spec.json](./api-spec.json) 或 [api-doc.html](./api-doc.html)。

---

## 五、已知缺陷

| # | 位置 | 描述 |
|---|------|------|
| 1 | ~~`AdminController.add`~~ | ✅ 已修复：用户名重复时原返回 `"用户名不存在"`，已改为 `"用户名已存在"` |
| 2 | ~~`AdminMapper.xml`~~ | ✅ 已修复：原缺少 `updatePassword` 的 SQL（调用 `PUT /api/admins/{id}/password` 会抛 `BindingException`），已补充 `<update>` 语句 |
| 3 | `application.yml` | 数据源账号密码硬编码（`Library_db` / `Library_db`），应移至环境变量 |
| 4 | ~~安全~~ | ✅ 已修复：原无登录拦截器，`/api/admins`、`/api/readers` 可被未认证访问；已新增 `AuthInterceptor` 统一拦截，未登录返回 401 |
| 5 | ~~`BookMapper`~~ | ✅ 已补充：`BookInfoMapper` 已创建（findById / queryAll / insert / update / updateStatus / deleteByIds），并新增 `BookController`（`/api/books`）提供图书 CRUD |
| 6 | `application.yml` | 数据库连接信息（库名 `library_db`、用户名、密码）仍为硬编码，建议迁移至环境变量或外部配置 |
| 7 | 细粒度权限 | 拦截器仅校验「是否登录」，未区分角色；如需「仅管理员可访问 `/api/admins`」需进一步扩展 `AuthInterceptor`（读取 `Contants.SESSION_TYPE` 比对） |

---

## 六、启动方式

```bash
# 1. 确保 MySQL 运行，并创建数据库
mysql -u root -p
CREATE DATABASE IF NOT EXISTS library_db DEFAULT CHARSET utf8mb4;

# 2. 导入建库建表 + 种子数据（admin / reader_info / book_info / lend_list / notice / type_info）
mysql -u root -p library_db < src/main/resources/mapper/Library.sql

# 3. 确认 application.yml 中的数据源配置（默认 library_db / Library_db / Library_db）

# 4. 启动
mvn spring-boot:run
```

启动后访问：
- API 文档：直接打开 [api-doc.html](./api-doc.html)（纯静态，无需启动后端）
- 后端服务：<http://localhost:8080>

---

## 七、初始数据

> 执行 `src/main/resources/mapper/Library.sql` 即可建库建表并导入全部种子数据。

种子账号（来自 Library.sql）：
- **管理员**：`admin / 12345`（adminType=1 超级管理员）、`qqwer / 12345`、`xy1222 / 12345`（普通）
- **读者**：`zhangsan / 12345`（彭于晏，编号 `8120116041`）、`mary / 12345`（陈恋）、`cindy / 12345`（辛帝）
- **图书**：6 条（西游记 / 三国演义 / 红楼梦 / 资本论 / 经济学原理 / 大变局下的中国法治）
- **借阅记录**：5 条（id 39 为未归还）
- **公告**：6 条　**图书分类**：6 类（文学 / 历史 / 工学 / 法学 / 经济学 / 统计学）

---

## 八、项目文档

| 文件 | 说明 |
|------|------|
| [README.md](./README.md) | 本文件 — 项目说明、机制、接口、缺陷、启动指南 |
| [api-spec.json](./api-spec.json) | OpenAPI 3.0 规范（可直接导入 Postman / Swagger Editor） |
| [api-doc.html](./api-doc.html) | 纯静态 API 文档（浏览器打开，无需联网） |
| [src/main/resources/mapper/Library.sql](./src/main/resources/mapper/Library.sql) | 建库建表 + 种子数据脚本 |
