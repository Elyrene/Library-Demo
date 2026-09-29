import { useUserStore } from '@/stores/user.ts'
import Login from '../views/Login.vue'
import { createRouter, createWebHistory } from 'vue-router'
import { api } from '@/api/index.ts'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'login',
      component: Login,
    },
    {
      path: '/home',
      name: 'home',
      component: () => import("../views/Home.vue"),
    },
    {
      path: '/regist',
      name: 'regist',
      component: () => import("../views/Regist.vue"),
    }
  ],
})

export default router

router.beforeEach(async (to) => {
  const userStore = useUserStore();
  if (!userStore.isLoginedIn) {
    const data = await api.get("/api/auth/me");
    // userStore.user = data.role;
  }
})
