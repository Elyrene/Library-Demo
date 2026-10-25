import { HttpClient } from "./handler";
import router from "@/router";
import { useUserStore } from "@/stores/user";

export const api = new HttpClient({
  baseURL: 'http://localhost:8080' ,
  timeout: 5000,
  onUnauthorized: () => {
    // 防止多个 401 请求同时触发多次弹窗
    if ((window as any).__unauthRedirecting) return;
    (window as any).__unauthRedirecting = true;
    const userStore = useUserStore();
    userStore.clearUser();
    // 纯提示弹窗（仅「确定」按钮），确认后跳登录页
    window.alert('登录已过期，请重新登录');
    router.push('/login');
    setTimeout(() => { (window as any).__unauthRedirecting = false; }, 1000);
  },
});

api.setRequestInterceptor((config) => {
//   console.log('Request Interceptor:', config);
  return config;
});

api.setResponseInterceptor((response) => {
//   console.log('Response Interceptor:', response);
  return response;
});

// interface RM {
//   '/posts/1': Post,
//   '/posts': Post[],
// }

// function req<K extends keyof RM>(url: K): Promise<RM[K]> {}

// let a = {a:1};
// a['a']

// req('/posts/1')

// // GET 请求（带响应类型）
// api.get<Post>('/posts/1')
//   .then((data) => console.log('GET /posts/1:', data))
//   .catch((err) => console.error('GET Error:', err));

// // POST 请求
// api
//   .post<Post>('/posts', { title: 'foo', body: 'bar', userId: 1 })
//   .then((data) => console.log('POST /posts:', data))
//   .catch((err) => console.error('POST Error:', err));

// // PUT 请求
// api
//   .put<Post>('/posts/1', { title: 'updated foo' })
//   .then((data) => console.log('PUT /posts/1:', data))
//   .catch((err) => console.error('PUT Error:', err));

// // DELETE 请求
// api
//   .delete<void>('/posts/1')
//   .then(() => console.log('DELETE /posts/1 succeeded'))
//   .catch((err) => console.error('DELETE Error:', err));