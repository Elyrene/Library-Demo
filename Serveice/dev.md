# 图书馆管理系统（Serveice）后续功能规划

> 基于 README.md、api-spec.json 与源码/数据库结构的审查结论。本文件只做功能建议，不包含代码。

## 一、缺失的核心业务模块（实体已存在但无接口）

### 1. 公告 Notice — 优先级最高
- `notice` 表已有 6 条种子数据，`Notice` 实体已存在，但完全没有对应的 Controller / Service / Mapper。每个真实的图书馆都需要公告功能（开馆时间、失物招领、闭馆通知等）。
- 建议接口：
  - `GET /api/notices` — 分页列表，按 `createDate` 倒序；支持按 `topic` / `content` 关键词筛选
  - `POST /api/notices` — 管理员发布公告
  - `PUT /api/notices/{id}` — 修改公告主题 / 内容
  - `DELETE /api/notices` — 批量删除
  - 可加 `PUT /api/notices/{id}/top` — 置顶公告（需加 `isTop` 字段）

### 2. 图书分类 Type
- `type_info` 表已有 6 个分类，但没有任何接口。图书通过 `typeId` 关联分类，管理员却无法通过系统管理分类。
- 建议接口：
  - `GET /api/types` — 全量列表（小表，无需分页）
  - `POST /api/types`、`PUT /api/types/{id}`、`DELETE /api/types` — CRUD
  - 删除时：若有图书引用该 `typeId` 则拒绝，或将相关图书的 `typeId` 置为 NULL

## 二、借阅业务缺口（影响真实可用性最大）

- **续借** — `PUT /api/lends/{id}/renew`：延长借阅期限。增加 `renewCount` 字段，限制续借次数（如最多 1 次），并更新应还日期。
- **逾期检测** — 在 `lend_list` 中增加 `dueDate` 字段。提供：
  - `GET /api/lends/overdue` — 管理员查看所有逾期记录
  - `GET /api/lends/my-overdue` — 读者查看自己的逾期书目
  - `GET /api/lends/near-due?days=3` — 即将到期（N 天内）的书目，用于提醒
- **罚款** — 在 `lend_list` 中增加 `fineAmount` 字段。还书时若 `backDate > dueDate` 则自动计算罚款。提供 `PUT /api/lends/{id}/fine` 调整罚款、`GET /api/readers/{id}/fines` 查询罚款记录。
- **每人借阅上限** — 在 `LendServiceImpl.lend()` 插入前，统计该读者当前未归还数量，若 `>= maxLends`（如 5 本）则拒绝。可在 `reader_info` 中增加 `maxLends` 或 `readerLevel` 字段。
- **按 ISBN 查询** — `GET /api/books/by-isbn/{isbn}`，方便柜台快速检索。

## 三、读者账户功能

- **修改自己的密码** — `PUT /api/readers/me/password`（仅限本人，需验证旧密码）。目前只有管理员能改密码。
- **编辑自己的资料** — `PUT /api/readers/me`（电话、邮箱、地址、性别、生日）。
- **读者列表 / 搜索** — `GET /api/readers` 分页 + 按 `username` / `realName` / `readerNumber` 模糊搜索。目前读者只有 `GET /{id}` 和注册，管理员无法浏览所有读者。
- **删除 / 禁用读者** — 管理员批量删除，或加 `status` 字段（启用 / 禁用），禁用后读者无法借阅。
- **读者详情含统计** — `GET /api/readers/{id}` 还应返回：累计借阅数、当前在借数、逾期数、累计罚款。

## 四、图书目录增强

- **封面图** — 增加 `coverUrl` 字段 + `POST /api/books/{id}/cover`（文件上传）或直接存 URL。对可浏览的目录是必需的。
- **库存 / 复本数** — 用 `totalCopies`（总册数）+ `availableCopies`（可借册数）替代单一的 `status`（0/1）。当前 `status==1` 会阻止同一本书的所有后续借阅，这是硬伤。
- **按作者 / ISBN 搜索** — 扩展 `GET /api/books` 参数：`author` 模糊、`isbn` 精确。
- **热门 / 新书推荐** — `GET /api/books/hot` 按借阅量排序（JOIN `lend_list` + GROUP BY + ORDER BY count DESC）；`GET /api/books/recently-added` 最近入库。
- **图书详情展示借阅记录** — `/api/lends/book/{bookId}` 已存在，前端在图书详情页直接展示即可。

## 五、安全与权限

- **基于角色的访问控制** — README 已知缺陷 #7 已指出。扩展 `AuthInterceptor` 读取 `SESSION_TYPE` 并强制：
  - `/api/admins/**` → 仅管理员
  - `/api/books/**` 写操作（POST / PUT / DELETE）→ 仅管理员；GET 对所有登录用户开放
  - `/api/lends` 的 POST / PUT / DELETE → 管理员；读者自己的借阅记录 → 读者本人
  - `/api/notices` 写操作 → 管理员；读 → 所有人
- **密码策略** — 注册允许 6–20 位但无复杂度要求。建议最低 8 位且须含字母和数字。
- **密码哈希** — 目前明文存储 / 比较（`admin/12345`）。即使不引入 Spring Security 全家桶，也应引入 `BCryptPasswordEncoder`。
- **Session 超时 + 记住我** — 设置 `spring.session.timeout`；可选 token 式"保持登录"。
- **审计日志** — 新增 `login_log` / `operation_log` 表，记录登录成功 / 失败、借书、还书、删除等关键操作。

## 六、数据与工程质量

- **修复读者编号 3 位溢出** — `ReaderController.generateReaderNumber()` 在第 1000 个读者时会生成 `81201161000`（4 位）。要么加宽格式，要么改用 DB 序列 / `reader_seq` 表。
- **外置数据库凭据** — `application.yml` 硬编码了 `Library_db/Library_db`（缺陷 #3/#6）。改用环境变量：`${DB_USER:Library_db}`。
- **校验注解** — `BookController.add` 和 `LendController` 用手动判空；应在 DTO 上加 `@NotBlank`、`@Min`、`@Email`（`validation` 依赖已在 pom 中）。
- **乐观锁** — `book_info` 和 `lend_list` 增加 `version` 字段，防止并发借 / 还时的丢失更新。
- **分页 limit 上限** — 将 `limit` 上限设为 100 之类，防止 `limit=1000000` 的 DoS。
- **API 版本化** — 路由前缀加 `/api/v1/...`，便于将来做破坏性变更。
- **单元 / 集成测试** — `ServeiceApplicationTests` 目前是空壳。补充 `@SpringBootTest` + `MockMvc` 测试，覆盖认证拦截器和借 / 还事务。

## 七、进阶 / 差异化功能

- **全文搜索** — 对书名 / 作者 / ISBN 做全文检索。这个量级 MySQL `FULLTEXT` 索引足够，无需引入 Elasticsearch。
- **数据导出** — `GET /api/lends/export?format=csv` 和 `GET /api/books/export`，供管理员做报表。
- **统计看板** — `GET /api/stats/overview`：总图书数、读者数、在借数、逾期数、热门 Top 10、月度借阅趋势。
- **读者自助服务** — 读者应能查看自己的借阅列表、续借自己的书、缴纳自己的罚款，无需管理员介入（`/api/lends/mine`）。
- **通知推送** — 在以下场景触发邮件 / 短信：借阅即将到期（3 天前）、已逾期、新公告。即使先写一个 `NotificationService` 接口空壳，也为将来接入具体服务商留好扩展点。

## 八、建议实施优先级（按"单位工作量的用户价值"排序）

1. **公告 + 分类接口**（实体已存在，纯接线工作）
2. **逾期 + dueDate + 续借**（让系统变成真正的流通系统）
3. **`AuthInterceptor` 中的角色权限控制**（修复缺陷 #7）
4. **读者列表 + 自助服务**（改密码、查自己的借阅）
5. **库存 / 复本模型**（解锁同书多册同时借出）
6. **罚款、统计看板、审计日志**

> 以上仅为建议，未写任何代码。如需将其中任何一项细化为接口规范或实现方案，可在此基础上继续。
