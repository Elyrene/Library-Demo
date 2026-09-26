import { HttpClient } from "./handler";

export const api = new HttpClient({
  baseURL: 'http://localhost:3000' ,
//   headers: {
//     Authorization: 'Bearer your_token_here',
//   },
  timeout: 5000,
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