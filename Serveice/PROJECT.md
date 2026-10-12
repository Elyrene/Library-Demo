# 图书馆管理系统 — 后端服务

基于 **Spring Boot 3 + MyBatis + MySQL** 的图书馆管理系统后端，提供管理员管理、读者注册/登录等 REST API。

---

## 技术栈

| 分类 | 技术 / 版本 |
|------|------------|
| 语言 | Java 17 |
| 框架 | Spring Boot 3.5.0 |
| ORM | MyBatis Spring Boot Starter 3.0.3 |
| 数据库 | MySQL（mysql-connector-j） |
| 分页 | PageHelper 2.1.1 |
| 参数校验 | spring-boot-starter-validation |
| 工具 | Lombok |
| 构建 | Maven（Maven Wrapper） |
| 服务端口 | `8080` |
| 数据库 | `library_db` @ `localhost:3306` |

---

## 项目结构

```
Serveice/
├── pom.xml
├── HELP.md
├── mvnw / mvnw.cmd
├── .mvn/wrapper/
└── src/
    ├── main/
    │   ├── java/com/Library/
    │   │   ├── app/
    │   │   │   └── ServeiceApplication.java      # 启动入口
    │   │   ├── common/
    │   │   │   ├── Contants.java                 # 常量（Session key）
    │   │   │   ├── DataInfo.java                 # 统一响应体
    │   │   │   └── GlobalExceptionHandler.java   # 全局异常处理
    │   │   ├── config/
    │   │   │   └── WebConfig.java                # CORS 配置
    │   │   ├── controller/
    │   │   │   ├── AdminController.java          # 管理员 CRUD
    │   │   │   ├── AuthController.java           # 登录/登出/当前用户
    │   │   │   ├── ReaderController.java         # 读者注册/查询
    │   │   │   └── dto/
    │   │   │       ├── LoginRequest.java         # 登录请求 DTO
    │   │   │       └── RegisterRequest.java      # 注册请求 DTO
    │   │   ├── entity/
    │   │   │   ├── Admin.java                    # 管理员实体
    │   │   │   └── ReaderInfo.java               # 读者实体
    │   │   └── mapper/
    │   │       ├── AdminMapper.java              # 管理员 Mapper 接口
    │   │       └── ReaderInfoMapper.java         # 读者 Mapper 接口
    │   └── resources/
    │       ├── application.yml                  # 应用配置
    │       └── mapper/
    │           ├── AdminMapper.xml              # 管理员 SQL
    │           ├── ReaderInfoMapper.xml         # 读者 SQL
    │           └── Library.sql                  # 数据库建表 + 初始数据
    └── test/
        └── java/com/Library/app/
            └── ServeiceApplicationTests.java
```

---

## 数据库设计

### `admin` — 管理员表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT, PK, AUTO_INCREMENT | 主键 |
| username | VARCHAR(20) | 用户名 |
| password | VARCHAR(20) | 密码 |
| adminType | INT | 管理员类型（0=普通, 1=超级管理员） |

**初始数据：**
- admin / 12345（超级管理员）
- qqwer / 12345（普通管理员）
- xy1222 / 12345（普通管理员）

### `reader_info` — 读者信息表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT, PK, AUTO_INCREMENT | 主键 |
| username | VARCHAR(20) | 用户名 |
| password | VARCHAR(20) | 密码 |
| realName | VARCHAR(20) | 真实姓名 |
| sex | VARCHAR(5) | 性别 |
| birthday | DATE | 出生日期 |
| address | VARCHAR(30) | 籍贯 |
| tel | VARCHAR(11) | 电话 |
| email | VARCHAR(15) | 邮箱 |
| registerDate | DATETIME | 注册日期 |
| readerNumber | VARCHAR(20) | 读者编号（自动生成，前缀 `8120116`） |

**初始数据：** zhangsan、mary、cindy（密码均为 12345）

### `book_info` — 图书信息表（已建表，暂无对应 API）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT, PK | 主键 |
| name | VARCHAR(20) | 图书名称 |
| author | VARCHAR(30) | 作者 |
| publish | VARCHAR(30) | 出版社 |
| isbn | VARCHAR(30) | 书籍编号 |
| introduction | VARCHAR(50) | 简介 |
| language | VARCHAR(20) | 语言 |
| price | DOUBLE | 价格 |
| publish_date | DATE | 出版时间 |
| type_id | INT | 书籍类型 |
| status | INT | 0=未借出, 1=已借出 |

---

## API 接口

### 1. 认证模块 `/api/auth`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 登录（区分管理员/读者） |
| POST | `/api/auth/logout` | 登出 |
| GET | `/api/auth/me` | 获取当前登录用户信息 |

#### 登录请求体 `LoginRequest`

```json
{
  "username": "admin",
  "password": "12345",
  "role": "admin"   // "admin" 或 "reader"
}
```

#### 登录成功响应

```json
{
  "code": 0,
  "mes": "ok"
}
```

#### 获取当前用户响应

```json
{
  "code": 0,
  "mes": "ok",
  "data": {
    "role": "admin",
    "user": {
      "id": 1,
      "username": "admin",
      "adminType": 1
    }
  }
}
```

---

### 2. 管理员模块 `/api/admins`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admins?pageNum=1&limit=10&username=&adminType=` | 分页查询管理员列表 |
| GET | `/api/admins/{id}` | 根据 ID 查询管理员 |
| POST | `/api/admins` | 新增管理员 |
| PUT | `/api/admins/{id}/password` | 修改密码 |
| DELETE | `/api/admins` | 批量删除（body: `[1, 2, 3]`） |

#### 分页查询响应

```json
{
  "code": 0,
  "mes": "ok",
  "count": 3,
  "data": [
    { "id": 1, "username": "admin", "adminType": 1 },
    { "id": 2, "username": "qqwer", "adminType": 0 }
  ]
}
```

---

### 3. 读者模块 `/api/readers`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/readers/{id}` | 根据 ID 查询读者 |
| POST | `/api/readers/register` | 读者注册 |

#### 注册请求体 `RegisterRequest`

```json
{
  "username": "zhangsan",
  "password": "123456",
  "realName": "张三",
  "sex": "男",
  "birthday": "2001-04-01",
  "address": "江西南昌",
  "tel": "13767134834",
  "email": "yu123@163.com"
}
```

> 密码长度 6–20 位，由 `@Size` 注解校验。

#### 读者编号生成规则

- 前缀固定为 `8120116`
- 流水号为 3 位，从 `001` 递增
- 示例：`8120116001`, `8120116002` …

---

## 统一响应格式 `DataInfo<T>`

```json
{
  "code": 0,       // 0=成功, -1=失败
  "mes": "ok",     // 消息
  "count": 10,     // 分页总数（仅分页接口）
  "data": { }       // 数据体
}
```

| code | 含义 |
|------|------|
| 0 | 成功 |
| -1 | 失败 |

---

## 全局异常处理

`GlobalExceptionHandler` 拦截所有 Controller 异常：

| 异常类型 | HTTP 状态码 | 响应 |
|----------|------------|------|
| `MethodArgumentNotValidException` | 200 | `DataInfo.fail(校验消息)` |
| `BindException` | 200 | `DataInfo.fail(校验消息)` |
| `ConstraintViolationException` | 400 | `DataInfo.fail(校验消息)` |
| `IllegalAccessException` | 400 | `DataInfo.fail(消息)` |
| `Exception`（兜底） | 500 | `DataInfo.fail("服务器内部错误")` |

---

## CORS 配置

- 允许来源：`http://localhost:5173`（前端 Vite 开发服务器）
- 允许方法：GET, POST, PUT, DELETE, OPTIONS
- 允许携带 Cookie（Session 认证）
- 预检缓存：3600 秒

---

## 认证方式

- **Session 认证**（基于 Cookie）
- 登录后将用户对象和角色存入 `HttpSession`
- 前端通过 `withCredentials: true` 携带 Cookie

---

## 运行方式

### 1. 初始化数据库

```sql
-- 执行 src/main/resources/mapper/Library.sql
CREATE DATABASE IF NOT EXISTS library_db DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE library_db;
source src/main/resources/mapper/Library.sql;
```

### 2. 修改数据库配置

编辑 `src/main/resources/application.yml`，将 `username` 和 `password` 改为你的 MySQL 账号。

### 3. 启动

```bash
# 使用 Maven Wrapper
./mvnw spring-boot:run        # Linux/Mac
mvnw.cmd spring-boot:run      # Windows
```

### 4. 访问

- 服务地址：`http://localhost:8080`

---

## 已知问题 / 待完善

1. **`AdminController.add()` 中用户名重复提示有误**：`DataInfo.fail("用户名不存在")` 应为 `"用户名已存在"`。
2. **`AdminMapper.xml` 缺少 `updatePassword` 的 SQL**：接口有该方法但 XML 中未定义，运行时会报 `BindingException`。
3. **`book_info` 表已建表**，但尚未有对应的 Entity / Mapper / Controller，借阅功能未实现。
4. **密码明文存储**，生产环境应使用 BCrypt 等加密方式。
5. **`Contants.java`** 类名有拼写错误（应为 `Constants`），且 `@Data` 注解用于常量类不太恰当（Lombok 会生成无用方法）。
6. **CORS 只允许 `localhost:5173`**，部署时需更新。
