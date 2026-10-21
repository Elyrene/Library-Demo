import { useUserStore } from '@/stores/user.ts'
import Login from '../views/Login.vue'
import { createRouter, createWebHistory } from 'vue-router'
import { api } from '@/api/index.ts'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: Login,
      meta: { public: true },
    },
    {
      path: '/regist',
      name: 'regist',
      component: () => import("../views/Regist.vue"),
      meta: { public: true },
    },
    {
      path: '/home',
      name: 'home',
      component: () => import("../views/Home.vue"),
      meta: { requiresAuth: true },
    },
    {
      path: '/',
      redirect: '/home',
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      redirect: '/home',
    },
  ],
})

// 全局路由守卫：登录拦截
router.beforeEach(async (to) => {
  const userStore = useUserStore();

  // 已登录但访问登录/注册页：回到首页，避免重复登录
  if (userStore.isLoggedIn && to.meta.public) {
    return { name: 'home' };
  }

  // 无需登录的公共页面（登录/注册）直接放行
  if (to.meta.public) return true;

  // 未登录：尝试用现有 cookie 恢复会话，失败则跳登录
  if (!userStore.isLoggedIn) {
    try {
      const data = await api.get<AuthMeRes>("/api/auth/me");
      userStore.setRole(data.data.role);
      if (data.data.role === 'admin') {
        userStore.setAdmin(data.data.user as adminInfo);
      } else {
        userStore.setReader(data.data.user as readerInfo);
      }
    } catch {
      userStore.clearUser();
      return { name: 'login' };
    }
  }

  return true;
})

export default router
