import { api } from "./index";

// 借阅记录相关接口的类型化封装，统一返回 DataInfo 结构（code/mes/count?/data）。
// 成功与否由调用方根据 res.code === 0 判断（后端业务失败时 code=-1，HTTP 仍为 200）。

/** 分页查询借阅记录（管理员）。支持书名/读者姓名模糊搜索、归还类型筛选、分页。 */
export function listLends(params?: {
  pageNum?: number;
  limit?: number;
  bookName?: string;
  readerName?: string;
  backType?: number | null;
}) {
  const query: Record<string, string | number | boolean | string[] | number[]> = {};
  if (params?.pageNum != null) query.pageNum = params.pageNum;
  if (params?.limit != null) query.limit = params.limit;
  if (params?.bookName) query.bookName = params.bookName;
  if (params?.readerName) query.readerName = params.readerName;
  if (params?.backType != null) query.backType = params.backType;
  return api.get<LendsRes>("/api/lends", query);
}

/** 读者借阅历史（不分页，按读者 ID）。 */
export function listLendsByReader(readerId: number) {
  return api.get<LendsPlainRes>(`/api/lends/reader/${readerId}`);
}

/** 图书借阅记录（不分页，按图书 ID）。 */
export function listLendsByBook(bookId: number) {
  return api.get<LendsPlainRes>(`/api/lends/book/${bookId}`);
}

/** 查询单条借阅详情。 */
export function getLendById(id: number) {
  return api.get<LendIdRes>(`/api/lends/${id}`);
}

/** 借书（bookId + readerId），后端置图书 status=1。 */
export function lendBook(bookId: number, readerId: number) {
  return api.post<LendPostRes>("/api/lends", { bookId, readerId });
}

/** 还书，设置归还日期/类型/备注，后端置图书 status=0。 */
export function backLend(id: number, body: { backType?: number; exceptRemarks?: string }) {
  return api.put<LendBackRes>(`/api/lends/${id}/back`, body);
}

/** 批量删除借阅记录（ID 数组，非空）。 */
export function deleteLends(ids: number[]) {
  // HttpClient 的 delete() 不携带请求体，而批量删除接口需要 body 为 ID 数组；
  // 底层 request('DELETE', ...) 会 JSON 序列化并携带 body，故直接调用。
  return api.request<LendDeleteRes>('DELETE', '/api/lends', JSON.stringify(ids));
}
