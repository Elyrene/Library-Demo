<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { listNotices } from '@/api/notices';

const loading = ref(false);
const error = ref('');
const list = ref<noticeInfo[]>([]);
const count = ref(0);
const pageNum = ref(1);
const limit = ref(10);
const totalPages = computed(() => Math.max(1, Math.ceil(count.value / limit.value)));

const selected = ref<noticeInfo | null>(null);

const fmtDate = (d?: string | null) => (d ? String(d).replace('T', ' ').slice(0, 16) : '—');

async function load() {
  loading.value = true;
  error.value = '';
  try {
    const data = await listNotices({ pageNum: pageNum.value, limit: limit.value });
    if (data.code === 0) {
      list.value = data.data || [];
      count.value = data.count ?? list.value.length;
    } else {
      error.value = data.mes || '加载公告失败';
    }
  } catch (e: any) {
    error.value = e?.message || '加载公告失败';
  } finally {
    loading.value = false;
  }
}

function onPrev() { if (pageNum.value > 1) { pageNum.value--; load(); } }
function onNext() { if (pageNum.value < totalPages.value) { pageNum.value++; load(); } }

onMounted(load);
</script>

<template>
  <div class="notices">
    <h1>公告</h1>

    <div class="card">
      <p v-if="loading" class="hint">加载中…</p>
      <p v-else-if="error" class="error">{{ error }}</p>
      <template v-else>
        <div v-if="list.length" class="notice-list">
          <button v-for="n in list" :key="n.id" class="notice-item" @click="selected = n">
            <div class="notice-title">{{ n.topic }}</div>
            <div class="notice-meta">
              <span class="notice-author">发布人：{{ n.author || '—' }}</span>
              <span class="notice-date">{{ fmtDate(n.createDate) }}</span>
            </div>
            <p class="notice-preview">{{ n.content }}</p>
          </button>
        </div>
        <p v-else class="hint">暂无公告</p>

        <div class="pager">
          <button class="btn-ghost" :disabled="pageNum <= 1 || loading" @click="onPrev">上一页</button>
          <span class="hint">第 {{ pageNum }} / {{ totalPages }} 页 · 共 {{ count }} 条</span>
          <button class="btn-ghost" :disabled="pageNum >= totalPages || loading" @click="onNext">下一页</button>
        </div>
      </template>
    </div>

    <div v-if="selected" class="modal-mask" @click.self="selected = null">
      <div class="modal">
        <h3>{{ selected.topic }}</h3>
        <p class="notice-meta">
          <span class="notice-author">发布人：{{ selected.author || '—' }}</span>
          <span class="notice-date">{{ fmtDate(selected.createDate) }}</span>
        </p>
        <p class="notice-content">{{ selected.content }}</p>
        <div class="modal-actions">
          <button class="btn-ghost" @click="selected = null">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.notices h1 {
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
  margin-bottom: 8px;
}
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.notice-item {
  width: 100%;
  text-align: left;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px 14px;
  background: #fff;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.notice-item:hover {
  border-color: #10b981;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.notice-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 6px;
}
.notice-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  font-size: 12px;
  color: #64748b;
  margin-bottom: 6px;
}
.notice-preview {
  margin: 0;
  color: #475569;
  font-size: 13px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.pager {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  margin-top: 16px;
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
}
.btn-ghost:hover:not(:disabled) {
  background: #e2e8f0;
}
.btn-ghost:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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
  max-width: 560px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
}
.modal h3 {
  margin: 0 0 12px;
  font-size: 16px;
  color: #1e293b;
}
.notice-content {
  margin: 0 0 16px;
  color: #334155;
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
}
</style>
