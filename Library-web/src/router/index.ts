import Login from '../views/Login.vue'
import { createRouter, createWebHistory } from 'vue-router'

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
