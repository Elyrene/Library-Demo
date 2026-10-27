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
    // Reader section
    {
      path: '/reader',
      component: () => import("../views/reader/ReaderLayout.vue"),
      meta: { requiresAuth: true, role: 'reader' },
      children: [
        {
          path: 'dashboard',
          name: 'reader-dashboard',
          component: () => import("../views/reader/Dashboard.vue"),
        },
        {
          path: 'profile',
          name: 'reader-profile',
          component: () => import("../views/reader/Profile.vue"),
        },
        {
          path: 'borrowed',
          name: 'reader-borrowed',
          component: () => import("../views/reader/BorrowedBooks.vue"),
        },
        {
          path: 'books',
          name: 'reader-books',
          component: () => import("../views/reader/BookBrowse.vue"),
        },
        {
          path: 'notices',
          name: 'reader-notices',
          component: () => import("../views/reader/Notices.vue"),
        },
      ],
    },
    // Admin section
    {
      path: '/admin',
      component: () => import("../views/admin/AdminLayout.vue"),
      meta: { requiresAuth: true, role: 'admin' },
      children: [
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: () => import("../views/admin/Dashboard.vue"),
        },
        {
          path: 'books',
          name: 'admin-books',
          component: () => import("../views/admin/Books.vue"),
        },
        {
          path: 'admins',
          name: 'admin-admins',
          component: () => import("../views/admin/Admins.vue"),
        },
        {
          path: 'readers',
          name: 'admin-readers',
          component: () => import("../views/admin/Readers.vue"),
        },
        {
          path: 'password',
          name: 'admin-password',
          component: () => import("../views/admin/ChangePassword.vue"),
        },
        {
          path: 'bookinfo',
          name: 'admin-bookinfo',
          component: () => import("../views/admin/BookInfo.vue"),
        },
        {
          path: 'lends',
          name: 'admin-lends',
          component: () => import("../views/admin/Books.vue"),
        },
        {
          path: 'notices',
          name: 'admin-notices',
          component: () => import("../views/admin/Notices.vue"),
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      redirect: '/home',
    },
  ],
})

// Global route guard: login interception
router.beforeEach(async (to) => {
  const userStore = useUserStore();

  // Logged in but visiting login/regist: go home
  if (userStore.isLoggedIn && to.meta.public) {
    return { name: 'home' };
  }

  // Public pages (login/regist) pass through
  if (to.meta.public) return true;

  // Not logged in: try to restore session from cookie, else redirect to login
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

  // Role check: reader/admin section access
  if (to.meta.role && userStore.role !== to.meta.role) {
    return { name: 'home' };
  }

  return true;
})

export default router
