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

// router.beforeEach(async (to) => {
//   const userStore = useUserStore();
//   if (!userStore.isLoggedIn) {
//     try {
//       const data = await api.get<AuthMeRes>("/api/auth/me");
//       userStore.setRole(data.data.role);
//       if(userStore.role === 'admin') {
//         userStore.setAdmin(data.data.user as adminInfo);
//       } else {
//         userStore.setReader(data.data.user as readerInfo);
//       }
//     } catch {
//       userStore.clearUser();
//     }

//   }
//   const needRole = to.meta.role as string | undefined;
//   if (needRole && userStore.role !== needRole) {
//     return userStore.role === 'admin' ? '/home' : '/home';
//   }
// })
