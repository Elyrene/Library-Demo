# 图书馆管理系统 — 后端服务（Serveice）

基于 **Spring Boot 3 + MyBatis + MySQL** 的图书馆管理系统后端，提供管理员与读者两种角色的认证、管理员 CRUD、读者注册与查询能力。

---

## 一、技术栈

| 组件 | 版本 / 说明 |
|------|------------|
| Java | 17 |
| Spring Boot | 3.x（web / validation / aop） |
| MyBatis | 2.3.x（spring-boot-starter） |
| MySQL | 8.0.33，驱动 `com.mysql.cj.jdbc.Driver` |
| PageHelper | 1.4.7（分页插件） |
| Lombok | 1.18.30（`@Data` 等） |
| 构建 | Maven |
| 数据库 | MySQL 8（`mydb`，UTF8MB4） |

---

## 二、项目结构

```
src/main/java/com/library/
├── LibraryServiceApplication.java        # 启动类（@MapperScan）
├── config/
│   ├── GlobalExceptionHandler.java       # 全局异常 → DataInfo
│   └── MyBatisPlusConfig.java            # PageHelper 分页拦截器
├── controller/
│   ├── AuthController.java               # /api/auth  登录 / 登出 / 当前用户
│   ├── AdminController.java              # /api/admins  管理员 CRUD
│   └── ReaderController.java             # /api/readers  读者注册 / 查询
├── dao/
│   ├── AdminMapper.java                  # 管理员 Mapper（5 个方法）
│   ├── ReaderInfoMapper.java             # 读者 Mapper（4 个方法）
│   └── BookMapper.java                   # 书籍 Mapper（仅定义，未使用）
├── entity/
│   ├── Admin.java                        # 管理员实体
│   └── ReaderInfo.java                   # 读者实体
├── pojo/
│   ├── DataInfo.java                     # 统一响应体
│   └── dto/
│       ├── LoginRequest.java             # 登录 DTO
│       └── RegisterRequest.java          # 注册 DTO
└── util/
    └── GenerateReaderNumberUtil.java     # 读者编号生成器

src/main/resources/
├── application.yml                       # 数据源 / 端口 / 日志
└── mapper/
    ├── AdminMapper.xml                   # 管理员 SQL
    ├── ReaderInfoMapper.xml              # 读者 SQL
    └── BookMapper.xml                    # 书籍 SQL（仅定义，未使用）
```

---

## 三、核心机制

### 3.1 统一响应体 `DataInfo<T>`

```json
{
  "code": 0,          // 0 = 成功，-1 = 失败
  "mes": "ok",
  "count": 10,        // 分页总数（仅分页接口）
  "data": { ... }
}
```

- 注解 `@JsonInclude(NON_NULL)`：`null` 字段不序列化。
- 静态工厂：`ok()` / `ok(data)` / `ok(data, count)` / `fail(mes)` / `fail(code, msg)`。

### 3.2 认证方式：Session（Cookie）

- 登录成功后，将用户对象 + 角色写入 `HttpSession`（key 分别为 `user`、`role`）。
- 后续请求需携带 Cookie（前端 `withCredentials: true`）。
- **无拦截器**：未登录访问受保护接口时，`session.getAttribute("user")` 返回 `null`，由 Controller 判断后返回 `DataInfo.fail("未登录")`。

### 3.3 分页机制（PageHelper）

```java
PageHelper.startPage(pageNum, limit);
List<Admin> list = adminMapper.findAll(admin);
long total = new PageInfo<>(list).getTotal();
```

- `pageNum` 从 **1** 开始，`limit` 为每页条数。
- 返回 `DataInfo.ok(list, total)`，前端据此计算总页数。

### 3.4 全局异常处理

`GlobalExceptionHandler` 捕获所有未处理异常，统一返回：

```json
{ "code": -1, "mes": "服务器内部错误，请稍后重试" }
```

### 3.5 读者编号生成

`GenerateReaderNumberUtil`：

- 前缀：`8120116`
- 后缀：从 `SELECT MAX(CAST(readerNumber AS UNSIGNED)) FROM reader_info` 取最大值，+1 后格式化为 **3 位**（`%03d`）。
- ⚠️ 当序号超过 999 时会溢出为 4 位（`1000`），后续递增正常。

---

## 四、接口列表

### 认证 `/api/auth`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/login` | 登录（按 role 区分管理员 / 读者） |
| POST | `/logout` | 登出（清除 Session） |
| GET | `/me` | 获取当前登录用户 |

### 管理员 `/api/admins`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `` | 分页查询（支持 username 模糊搜索、adminType 筛选） |
| GET | `/{id}` | 按 ID 查询 |
| POST | `` | 新增（adminType 默认 1） |
| PUT | `/{id}/password` | 修改密码 |
| DELETE | `` | 批量删除（body: `List<Integer>`） |

### 读者 `/api/readers`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/{id}` | 按 ID 查询 |
| POST | `/register` | 注册（自动生成读者编号） |

> 完整参数 / 请求体 / 响应示例见 [api-spec.json](./api-spec.json) 或 [api-doc.html](./api-doc.html)。

---

## 五、已知缺陷

| # | 位置 | 描述 |
|---|------|------|
| 1 | `AdminController.addAdmin` | 用户名重复时返回 `"用户名不存在"`，应为 `"用户名已存在"` |
| 2 | `AdminMapper.xml` | **缺少 `updatePassword` 的 SQL**，调用 `PUT /api/admins/{id}/password` 会抛 `BindingException`（HTTP 500） |
| 3 | `application.yml` | 数据源密码硬编码（`123456`），应移至环境变量 |
| 4 | 安全 | 无登录拦截器，未登录访问 `/api/auth/me` 仅返回 `DataInfo.fail`，其他接口理论上可被未认证访问（取决于 Controller 内是否校验 Session） |
| 5 | `BookMapper` | 已定义但完全未使用（无对应 Service / Controller） |

---

## 六、启动方式

```bash
# 1. 确保 MySQL 运行，并创建数据库
mysql -u root -p
CREATE DATABASE IF NOT EXISTS mydb DEFAULT CHARSET utf8mb4;

# 2. 导入初始数据（含 admin 表 + 样例读者）
mysql -u root -p mydb < src/main/resources/sql/init_data.sql

# 3. 修改 application.yml 中的数据源密码（如需要）

# 4. 启动
mvn spring-boot:run
```

启动后访问：
- API 文档：<http://localhost:8080>（需前端）或直接看 [api-doc.html](./api-doc.html)
- Swagger（未启用）

---

## 七、初始数据

`src/main/resources/sql/init_data.sql`：

- **管理员**：`admin / admin123`（adminType=1）
- **读者**：`zhangsan / 123456`（张三，编号 `8120116001`）

---

## 八、文档

| 文件 | 说明 |
|------|------|
| [PROJECT.md](./PROJECT.md) | 项目完整分析报告（结构 / 功能 / 缺陷 / 建议） |
| [api-spec.json](./api-spec.json) | OpenAPI 3.0 规范（可直接导入 Postman / Swagger Editor） |
| [api-doc.html](./api-doc.html) | Redoc 交互式文档（浏览器打开） |
| [README.md](./README.md) | 本文件 |
