<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { listBooks } from '@/api/books';
import { useUserStore } from '@/stores/user';

const userStore = useUserStore();
const reader = userStore.user as readerInfo | null;

const loading = ref(false);
const error = ref('');
const list = ref<bookInfo[]>([]);
const count = ref(0);
const pageNum = ref(1);
const limit = ref(10);
const totalPages = computed(() => Math.max(1, Math.ceil(count.value / limit.value)));

const name = ref('');
const typeId = ref<number | null>(null);

// 详情弹窗
const detail = ref<bookInfo | null>(null);

// 借书记录（用于显示是否已借）
const myBorrowedIds = ref<Set<number>>(new Set());

const fmtDate = (d?: string | null) => (d ? String(d).replace('T', ' ').slice(0, 10) : '—');

async function load() {
  loading.value = true;
  error.value = '';
  try {
    const data = await listBooks({
      pageNum: pageNum.value,
      limit: limit.value,
      name: name.value || undefined,
      typeId: typeId.value ?? undefined,
    });
    if (data.code === 0) {
      list.value = data.data || [];
      count.value = data.count ?? list.value.length;
    } else {
      error.value = data.mes || '加载失败';
    }
  } catch (e: any) {
    error.value = e?.message || '加载失败';
  } finally {
    loading.value = false;
  }
}

async function loadMyBorrows() {
  if (!reader) return;
  try {
    const { listLendsByReader } = await import('@/api/lends');
    const data = await listLendsByReader(reader.id);
    if (data.code === 0) {
      const ids = new Set<number>();
      (data.data || []).forEach((l: lendInfo) => {
        if (!l.backDate) ids.add(l.bookId);
      });
      myBorrowedIds.value = ids;
    }
  } catch {
    // ignore
  }
}

function onFilter() {
  pageNum.value = 1;
  load();
}
function onReset() {
  name.value = '';
  typeId.value = null;
  pageNum.value = 1;
  load();
}
function onPrev() { if (pageNum.value > 1) { pageNum.value--; load(); } }
function onNext() { if (pageNum.value < totalPages.value) { pageNum.value++; load(); } }

function isBorrowed(b: bookInfo) {
  return myBorrowedIds.value.has(b.id) || b.status === 1;
}

onMounted(() => {
  load();
  loadMyBorrows();
});
</script>

<template>
  <div class="browse">
    <h1>图书浏览</h1>

    <!-- 筛选 -->
    <div class="card">
      <div class="filter-row">
        <input v-model="name" type="text" placeholder="书名 / 作者（模糊搜索）" class="input" />
        <input v-model.number="typeId" type="number" min="0" placeholder="分类 ID（选填）" class="input narrow" />
        <button class="btn" :disabled="loading" @click="onFilter">
          {{ loading ? '查询中…' : '搜索' }}
        </button>
        <button class="btn-ghost" :disabled="loading" @click="onReset">重置</button>
      </div>
    </div>

    <!-- 列表 -->
    <div class="card">
      <p v-if="loading" class="hint">加载中…</p>
      <p v-else-if="error" class="error">{{ error }}</p>
      <template v-else>
        <table v-if="list.length" class="table">
          <thead>
            <tr>
              <th>书名</th>
              <th>作者</th>
              <th>出版社</th>
              <th>出版日期</th>
              <th>语言</th>
              <th>价格</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in list" :key="r.id">
              <td class="col-name">{{ r.name }}</td>
              <td>{{ r.author }}</td>
              <td>{{ r.publish || '—' }}</td>
              <td>{{ fmtDate(r.publishDate) }}</td>
              <td>{{ r.language || '—' }}</td>
              <td>{{ r.price != null ? '¥' + r.price.toFixed(2) : '—' }}</td>
              <td>
                <span class="tag" :class="r.status === 0 ? 'open' : 'backed'">
                  {{ r.status === 0 ? '可借' : '已借出' }}
                </span>
              </td>
              <td>
                <button class="btn-small" @click="detail = r">详情</button>
              </td>
            </tr>
          </tbody>
        </table>
        <p v-else class="hint">暂无图书数据</p>
      </template>

      <!-- 分页 -->
      <div class="pager">
        <button class="btn-ghost" :disabled="pageNum <= 1 || loading" @click="onPrev">上一页</button>
        <span class="hint">第 {{ pageNum }} / {{ totalPages }} 页 · 共 {{ count }} 本</span>
        <button class="btn-ghost" :disabled="pageNum >= totalPages || loading" @click="onNext">下一页</button>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <div v-if="detail" class="modal-mask" @click.self="detail = null">
      <div class="modal modal-wide">
        <h3>{{ detail.name }}</h3>
        <div class="detail-grid">
          <div class="detail-item"><span class="label">作者</span><span class="value">{{ detail.author || '—' }}</span></div>
          <div class="detail-item"><span class="label">出版社</span><span class="value">{{ detail.publish || '—' }}</span></div>
          <div class="detail-item"><span class="label">ISBN</span><span class="value">{{ detail.isbn || '—' }}</span></div>
          <div class="detail-item"><span class="label">语言</span><span class="value">{{ detail.language || '—' }}</span></div>
          <div class="detail-item"><span class="label">价格</span><span class="value">{{ detail.price != null ? '¥' + detail.price.toFixed(2) : '—' }}</span></div>
          <div class="detail-item"><span class="label">出版日期</span><span class="value">{{ fmtDate(detail.publishDate) }}</span></div>
          <div class="detail-item"><span class="label">分类 ID</span><span class="value">{{ detail.typeId }}</span></div>
          <div class="detail-item">
            <span class="label">状态</span>
            <span class="value">
              <span class="tag" :class="detail.status === 0 ? 'open' : 'backed'">
                {{ detail.status === 0 ? '可借' : '已借出' }}
              </span>
            </span>
          </div>
        </div>
        <div v-if="detail.introduction" class="intro">
          <h4>图书简介</h4>
          <p>{{ detail.introduction }}</p>
        </div>
        <div class="modal-actions">
          <button class="btn-ghost" @click="detail = null">关闭</button>
          <router-link v-if="detail.status === 0 && reader" :to="`/reader/borrowed`" class="btn">去借阅</router-link>
          <span v-else-if="detail.status === 1" class="hint">该图书已借出，暂不可借</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.browse h1 {
  font-size: 20px;
  color: #1e293b;
  margin-bottom: 12px;
}
.hint {
  color: #94a3b8;
  font-size: 14px;
}
.error {
  color: #ef4444;
  font-size: 14px;
  margin-bottom: 8px;
}
.card {
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  margin-bottom: 16px;
  overflow-x: auto;
}
.filter-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.input {
  flex: 1;
  min-width: 120px;
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
  background: #fff;
}
.input:focus {
  border-color: #10b981;
}
.narrow {
  flex: 0 0 auto;
  width: 160px;
}
.btn {
  padding: 8px 20px;
  background: #10b981;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
  white-space: nowrap;
  flex: 0 0 auto;
  text-decoration: none;
}
.btn:hover:not(:disabled) {
  background: #059669;
}
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.btn-small {
  padding: 4px 12px;
  background: #10b981;
  color: #fff;
  border: none;
  border-radius: 5px;
  font-size: 12px;
  cursor: pointer;
  transition: background 0.2s;
}
.btn-small:hover {
  background: #059669;
}
.btn-ghost {
  padding: 8px 20px;
  background: #f1f5f9;
  color: #475569;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
  white-space: nowrap;
  text-decoration: none;
}
.btn-ghost:hover:not(:disabled) {
  background: #e2e8f0;
}
.btn-ghost:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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
.col-name {
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}
.tag.open {
  background: #d1fae5;
  color: #047857;
}
.tag.backed {
  background: #fef3c7;
  color: #b45309;
}
.pager {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  margin-top: 16px;
}
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
}
.modal {
  background: #fff;
  border-radius: 10px;
  padding: 20px 24px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
}
.modal-wide {
  max-width: 560px;
}
.modal h3 {
  margin: 0 0 16px;
  font-size: 18px;
  color: #1e293b;
}
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px 20px;
}
.detail-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  padding: 4px 0;
  border-bottom: 1px solid #f1f5f9;
}
.detail-item .label {
  color: #64748b;
}
.detail-item .value {
  color: #1e293b;
  font-weight: 500;
}
.intro {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
}
.intro h4 {
  margin: 0 0 6px;
  font-size: 14px;
  color: #64748b;
}
.intro p {
  margin: 0;
  font-size: 14px;
  color: #1e293b;
  line-height: 1.6;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
  align-items: center;
}
</style>
