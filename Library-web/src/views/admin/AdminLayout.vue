<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { useLogout } from '@/composables/useLogout';

const router = useRouter();
const userStore = useUserStore();
const { logout } = useLogout();

const adminTypeLabel = (t: number | undefined) =>
  t === 1 ? '馆长' : t === 0 ? '图书管理员' : (t ?? '');
</script>

<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <h2>管理后台</h2>
        <p class="username">{{ userStore.user?.username }}</p>
        <p class="role">{{ adminTypeLabel((userStore.user as adminInfo | null)?.adminType) }}</p>
      </div>
      <nav class="sidebar-nav">
        <router-link to="/admin/dashboard" class="nav-item">控制台</router-link>
        <router-link to="/admin/bookinfo" class="nav-item">图书管理</router-link>
        <router-link to="/admin/lends" class="nav-item">借阅管理</router-link>
        <router-link to="/admin/admins" class="nav-item">管理员管理</router-link>
        <router-link to="/admin/password" class="nav-item">修改密码</router-link>
      </nav>
      <div class="sidebar-footer">
        <button class="logout-btn" @click="logout">退出登录</button>
      </div>
    </aside>
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
  background: #f1f5f9;
}
.sidebar {
  width: 220px;
  background: #1e293b;
  color: #fff;
  display: flex;
  flex-direction: column;
  position: fixed;
  height: 100vh;
}
.sidebar-header {
  padding: 20px 16px;
  border-bottom: 1px solid #334155;
}
.sidebar-header h2 {
  margin: 0;
  font-size: 18px;
  color: #fff;
}
.username {
  margin: 4px 0 0;
  font-size: 13px;
  color: #94a3b8;
}
.role {
  margin: 2px 0 0;
  font-size: 12px;
  color: #64748b;
}
.sidebar-nav {
  flex: 1;
  padding: 12px 0;
}
.nav-item {
  display: block;
  padding: 10px 16px;
  color: #cbd5e1;
  text-decoration: none;
  font-size: 14px;
  transition: background 0.2s, color 0.2s;
}
.nav-item:hover {
  background: #334155;
  color: #fff;
}
.nav-item.router-link-active {
  background: #334155;
  color: #fff;
  border-left: 3px solid #10b981;
}
.sidebar-footer {
  padding: 12px 16px;
  border-top: 1px solid #334155;
}
.logout-btn {
  width: 100%;
  padding: 8px 0;
  background: #ef4444;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
}
.logout-btn:hover {
  background: #dc2626;
}
.main-content {
  flex: 1;
  margin-left: 220px;
  padding: 24px;
}
</style>
