<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useUserStore } from '@/stores/user';
import { listBooks } from '@/api/books';
import { listLendsByReader } from '@/api/lends';

const userStore = useUserStore();
const reader = computed(() => userStore.user as readerInfo | null);

// 图书推荐：当前可借的图书
const recBooks = ref<bookInfo[]>([]);
const recLoading = ref(false);
const recError = ref('');

// 我的借阅
const myBorrows = ref<lendInfo[]>([]);
const borrowLoading = ref(false);
const borrowError = ref('');

const fmtDate = (d?: string | null) => (d ? String(d).replace('T', ' ').slice(0, 16) : '—');

async function loadRecs() {
  recLoading.value = true;
  recError.value = '';
  try {
    const data = await listBooks({ pageNum: 1, limit: 6, status: 0 });
    if (data.code === 0) {
      recBooks.value = data.data || [];
    } else {
      recError.value = data.mes || '加载推荐失败';
    }
  } catch (e: any) {
    recError.value = e?.message || '加载推荐失败';
  } finally {
    recLoading.value = false;
  }
}

async function loadBorrows() {
  if (!reader.value) return;
  borrowLoading.value = true;
  borrowError.value = '';
  try {
    const data = await listLendsByReader(reader.value.id);
    if (data.code === 0) {
      myBorrows.value = data.data || [];
    } else {
      borrowError.value = data.mes || '加载借阅记录失败';
    }
  } catch (e: any) {
    borrowError.value = e?.message || '加载借阅记录失败';
  } finally {
    borrowLoading.value = false;
  }
}

const activeBorrows = computed(() => myBorrows.value.filter((l) => !l.backDate));
const returnedBorrows = computed(() => myBorrows.value.filter((l) => l.backDate));

onMounted(() => {
  loadRecs();
  loadBorrows();
});
</script>

<template>
  <div class="dashboard">
    <h1>读者控制台</h1>

    <!-- 欢迎卡片 -->
    <div class="card welcome-card">
      <div class="welcome">
        <div class="welcome-icon">📚</div>
        <div class="welcome-text">
          <p class="welcome-title">欢迎回来，{{ reader?.realName || reader?.username || '读者' }}</p>
          <p class="welcome-sub">读者编号：{{ reader?.readerNumber || '—' }}</p>
        </div>
      </div>
      <div class="welcome-stats">
        <div class="stat">
          <span class="stat-num">{{ activeBorrows.length }}</span>
          <span class="stat-label">在借图书</span>
        </div>
        <div class="stat">
          <span class="stat-num">{{ returnedBorrows.length }}</span>
          <span class="stat-label">已归还</span>
        </div>
        <div class="stat">
          <span class="stat-num">{{ myBorrows.length }}</span>
          <span class="stat-label">借阅总数</span>
        </div>
      </div>
    </div>

    <!-- 图书推荐 -->
    <div class="card">
      <div class="section-header">
        <h2>📖 新书推荐</h2>
        <router-link to="/reader/books" class="more-link">查看全部 →</router-link>
      </div>
      <p v-if="recLoading" class="hint">加载中…</p>
      <p v-else-if="recError" class="error">{{ recError }}</p>
      <div v-else-if="recBooks.length" class="rec-grid">
        <div v-for="b in recBooks" :key="b.id" class="rec-card">
          <div class="rec-cover">📕</div>
          <div class="rec-info">
            <p class="rec-name">{{ b.name }}</p>
            <p class="rec-author">{{ b.author || '—' }}</p>
            <p class="rec-publish">{{ b.publish || '—' }}</p>
            <div class="rec-meta">
              <span v-if="b.price != null" class="rec-price">¥{{ b.price.toFixed(2) }}</span>
              <span v-if="b.language" class="rec-lang">{{ b.language }}</span>
            </div>
          </div>
        </div>
      </div>
      <p v-else class="hint">暂无可借图书</p>
    </div>

    <!-- 我的借阅 -->
    <div class="card">
      <div class="section-header">
        <h2>📋 我的借阅</h2>
        <router-link to="/reader/borrowed" class="more-link">管理借阅 →</router-link>
      </div>
      <p v-if="borrowLoading" class="hint">加载中…</p>
      <p v-else-if="borrowError" class="error">{{ borrowError }}</p>
      <template v-else>
        <table v-if="myBorrows.length" class="table">
          <thead>
            <tr>
              <th>图书</th>
              <th>借出日期</th>
              <th>状态</th>
              <th>归还日期</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in myBorrows" :key="r.id">
              <td>{{ r.bookName || '—' }}</td>
              <td>{{ fmtDate(r.lendDate) }}</td>
              <td>
                <span class="tag" :class="r.backDate ? 'backed' : 'open'">
                  {{ r.backDate ? '已归还' : '借出中' }}
                </span>
              </td>
              <td>{{ fmtDate(r.backDate) }}</td>
            </tr>
          </tbody>
        </table>
        <p v-else class="hint">暂无借阅记录</p>
      </template>
    </div>
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
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
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
.welcome-stats {
  display: flex;
  gap: 24px;
}
.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 60px;
}
.stat-num {
  font-size: 24px;
  font-weight: 700;
  color: #10b981;
}
.stat-label {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.section-header h2 {
  margin: 0;
  font-size: 16px;
  color: #1e293b;
}
.more-link {
  font-size: 13px;
  color: #10b981;
  text-decoration: none;
}
.more-link:hover {
  text-decoration: underline;
}
.rec-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 12px;
}
.rec-card {
  display: flex;
  gap: 10px;
  padding: 12px;
  border: 1px solid #f1f5f9;
  border-radius: 8px;
  transition: box-shadow 0.2s;
}
.rec-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.rec-cover {
  font-size: 32px;
  flex-shrink: 0;
}
.rec-info {
  flex: 1;
  min-width: 0;
}
.rec-name {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.rec-author {
  margin: 2px 0 0;
  font-size: 12px;
  color: #64748b;
}
.rec-publish {
  margin: 1px 0 0;
  font-size: 12px;
  color: #94a3b8;
}
.rec-meta {
  display: flex;
  gap: 8px;
  margin-top: 6px;
}
.rec-price {
  font-size: 12px;
  color: #ef4444;
  font-weight: 600;
}
.rec-lang {
  font-size: 11px;
  background: #f1f5f9;
  color: #64748b;
  padding: 1px 6px;
  border-radius: 4px;
}
.table {
  width: 100%;
  border-collapse: collapse;
}
.table th,
.table td {
  text-align: left;
  padding: 10px 12px;
  border-bottom: 1px solid #f1f5f9;
  font-size: 14px;
}
.table th {
  color: #64748b;
  font-weight: 500;
  background: #f8fafc;
}
.table td {
  color: #1e293b;
}
.table tr:hover td {
  background: #f8fafc;
}
.tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}
.tag.open {
  background: #fef3c7;
  color: #b45309;
}
.tag.backed {
  background: #d1fae5;
  color: #047857;
}
</style>
