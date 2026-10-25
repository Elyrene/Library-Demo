<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useUserStore } from '@/stores/user';
import { listBooks } from '@/api/books';
import { listLends } from '@/api/lends';

const userStore = useUserStore();
const admin = userStore.user as adminInfo | null;

const loading = ref(false);
const error = ref('');

const totalBooks = ref(0);
const availableBooks = ref(0);
const borrowedBooks = ref(0);
const totalLends = ref(0);
const activeLends = ref(0);

async function load() {
  loading.value = true;
  error.value = '';
  try {
    const [booksRes, lendsRes] = await Promise.all([
      listBooks({ pageNum: 1, limit: 1 }),
      listLends({ pageNum: 1, limit: 1 }),
    ]);

    if (booksRes.code === 0) {
      totalBooks.value = booksRes.count ?? 0;
      // 需要分别查询可借和已借出数量
      const [availRes, borrowedRes] = await Promise.all([
        listBooks({ pageNum: 1, limit: 1, status: 0 }),
        listBooks({ pageNum: 1, limit: 1, status: 1 }),
      ]);
      if (availRes.code === 0) availableBooks.value = availRes.count ?? 0;
      if (borrowedRes.code === 0) borrowedBooks.value = borrowedRes.count ?? 0;
    }

    if (lendsRes.code === 0) {
      totalLends.value = lendsRes.count ?? 0;
      // 借出中 = 未归还
      const activeRes = await listLends({ pageNum: 1, limit: 1 });
      if (activeRes.code === 0) {
        activeLends.value = activeRes.count ?? 0;
      }
    }
  } catch (e: any) {
    error.value = e?.message || '加载统计数据失败';
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <div class="dashboard">
    <h1>管理员控制台</h1>

    <p v-if="loading" class="hint">加载中…</p>
    <p v-else-if="error" class="error">{{ error }}</p>

    <template v-else>
      <!-- 欢迎卡片 -->
      <div class="card welcome-card">
        <div class="welcome">
          <div class="welcome-icon">👨‍💼</div>
          <div class="welcome-text">
            <p class="welcome-title">欢迎，{{ admin?.username || '管理员' }}</p>
            <p class="welcome-sub">
              {{ admin?.adminType === 1 ? '馆长' : admin?.adminType === 0 ? '图书管理员' : '管理员' }}
            </p>
          </div>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon book">📚</div>
          <div class="stat-info">
            <span class="stat-num">{{ totalBooks }}</span>
            <span class="stat-label">图书总数</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon avail">✅</div>
          <div class="stat-info">
            <span class="stat-num">{{ availableBooks }}</span>
            <span class="stat-label">可借图书</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon borrowed">📖</div>
          <div class="stat-info">
            <span class="stat-num">{{ borrowedBooks }}</span>
            <span class="stat-label">已借出</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon lend">📋</div>
          <div class="stat-info">
            <span class="stat-num">{{ totalLends }}</span>
            <span class="stat-label">借阅记录</span>
          </div>
        </div>
      </div>

      <!-- 快捷操作 -->
      <div class="card">
        <div class="section-header">
          <h2>快捷操作</h2>
        </div>
        <div class="actions-row">
          <router-link to="/admin/bookinfo" class="action-btn">
            <span class="action-icon">📕</span>
            <span class="action-text">图书管理</span>
          </router-link>
          <router-link to="/admin/lends" class="action-btn">
            <span class="action-icon">📋</span>
            <span class="action-text">借阅管理</span>
          </router-link>
          <router-link to="/admin/admins" class="action-btn">
            <span class="action-icon">👥</span>
            <span class="action-text">管理员管理</span>
          </router-link>
          <router-link to="/admin/readers" class="action-btn">
            <span class="action-icon">🧑</span>
            <span class="action-text">读者管理</span>
          </router-link>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.dashboard {
  padding: 8px 0;
}
.dashboard h1 {
  font-size: 20px;
  color: #1e293b;
  margin-bottom: 12px;
}
.card {
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  margin-bottom: 16px;
  overflow-x: auto;
}
.hint {
  color: #94a3b8;
  font-size: 14px;
}
.error {
  color: #ef4444;
  font-size: 14px;
}
.welcome-card {
  display: flex;
  align-items: center;
}
.welcome {
  display: flex;
  align-items: center;
  gap: 12px;
}
.welcome-icon {
  font-size: 36px;
}
.welcome-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}
.welcome-sub {
  margin: 4px 0 0;
  font-size: 13px;
  color: #94a3b8;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}
.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 12px;
}
.stat-icon {
  font-size: 28px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  flex-shrink: 0;
}
.stat-icon.book { background: #dbeafe; }
.stat-icon.avail { background: #d1fae5; }
.stat-icon.borrowed { background: #fef3c7; }
.stat-icon.lend { background: #e0e7ff; }
.stat-info {
  display: flex;
  flex-direction: column;
}
.stat-num {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
}
.stat-label {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}
.section-header {
  margin-bottom: 12px;
}
.section-header h2 {
  margin: 0;
  font-size: 16px;
  color: #1e293b;
}
.actions-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  text-decoration: none;
  color: #1e293b;
  font-size: 14px;
  transition: background 0.2s, border-color 0.2s;
}
.action-btn:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}
.action-icon {
  font-size: 18px;
}
</style>
