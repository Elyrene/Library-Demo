import { api } from "./index";

// 公告（Notice）相关接口的类型化封装，统一返回 DataInfo 结构（code/mes/count?/data）。
// 成功与否由调用方根据 res.code === 0 判断（后端业务失败时 code=-1，HTTP 仍为 200）。

/** 分页查询公告列表（登录用户）。按发布时间倒序，支持主题/内容模糊搜索。 */
export function listNotices(params?: {
  pageNum?: number;
  limit?: number;
  topic?: string;
  content?: string;
}) {
  const query: Record<string, string | number | boolean | string[] | number[]> = {};
  if (params?.pageNum != null) query.pageNum = params.pageNum;
  if (params?.limit != null) query.limit = params.limit;
  if (params?.topic) query.topic = params.topic;
  if (params?.content) query.content = params.content;
  return api.get<NoticesRes>("/api/notices", query);
}

/** 查询单条公告详情。 */
export function getNoticeById(id: number) {
  return api.get<NoticeIdRes>(`/api/notices/${id}`);
}

/** 发布公告（仅管理员）。author 不传默认 admin；createDate 不传默认当前时间。 */
export function addNotice(body: {
  topic: string;
  content: string;
  author?: string;
  createDate?: string;
}) {
  return api.post<NoticePostRes>("/api/notices", body);
}

/** 修改公告主题 / 内容（仅管理员）。 */
export function updateNotice(id: number, body: {
  topic: string;
  content: string;
  author?: string;
  createDate?: string;
}) {
  return api.put<NoticePutRes>(`/api/notices/${id}`, body);
}

/** 批量删除公告（ID 数组，非空，仅管理员）。 */
export function deleteNotices(ids: number[]) {
  return api.request<NoticeDeleteRes>('DELETE', '/api/notices', ids as unknown as Record<string, unknown>);
}
