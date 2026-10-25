import { api } from "./index";

// 图书（BookInfo）相关接口的类型化封装，统一返回 DataInfo 结构（code/mes/count?/data）。
// 成功与否由调用方根据 res.code === 0 判断（后端业务失败时 code=-1，HTTP 仍为 200）。

/** 分页查询图书列表（管理员/读者）。支持书名/分类/状态筛选与分页。 */
export function listBooks(params?: {
  pageNum?: number;
  limit?: number;
  name?: string;
  typeId?: number;
  status?: number;
}) {
  const query: Record<string, string | number | boolean | string[] | number[]> = {};
  if (params?.pageNum != null) query.pageNum = params.pageNum;
  if (params?.limit != null) query.limit = params.limit;
  if (params?.name) query.name = params.name;
  if (params?.typeId != null) query.typeId = params.typeId;
  if (params?.status != null) query.status = params.status;
  return api.get<BooksRes>("/api/books", query);
}

/** 查询单本图书详情。 */
export function getBookById(id: number) {
  return api.get<BookIdRes>(`/api/books/${id}`);
}

/** 新增图书（status 默认 0 未借出）。 */
export function addBook(body: {
  name: string;
  author: string;
  publish: string;
  isbn: string;
  introduction: string;
  language: string;
  price: number;
  publishDate: string;
  typeId: number;
}) {
  return api.post<BookPostRes>("/api/books", body);
}

/** 修改图书信息。 */
export function updateBook(id: number, body: {
  name: string;
  author: string;
  publish: string;
  isbn: string;
  introduction: string;
  language: string;
  price: number;
  publishDate: string;
  typeId: number;
}) {
  return api.put<BookPutRes>(`/api/books/${id}`, body);
}

/** 批量删除图书（ID 数组，非空）。 */
export function deleteBooks(ids: number[]) {
  // HttpClient 的 delete() 不携带请求体，而批量删除接口需要 body 为 ID 数组；
  // 底层 request('DELETE', ...) 会 JSON 序列化并携带 body，故直接调用。
  return api.request<BookDeleteRes>('DELETE', '/api/books', JSON.stringify(ids));
}
