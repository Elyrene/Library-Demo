<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { listNotices, addNotice, updateNotice, deleteNotices } from '@/api/notices';

const loading = ref(false);
const error = ref('');
const list = ref<noticeInfo[]>([]);
const count = ref(0);
const pageNum = ref(1);
const limit = ref(10);
const totalPages = computed(() => Math.max(1, Math.ceil(count.value / limit.value)));

// 筛选
const topic = ref('');
const content = ref('');

// 新增/编辑弹窗
const formOpen = ref(false);
const editing = ref<noticeInfo | null>(null);
const form = ref({ topic: '', content: '' });
const formBusy = ref(false);
const formMsg = ref('');

// 批量删除
const selected = ref<number[]>([]);
const delMsg = ref('');
const delBusy = ref(false);

function openAdd() {
  formOpen.value = true;
  editing.value = null;
  form.value = { topic: '', content: '' };
  formMsg.value = '';
}

function openEdit(row: noticeInfo) {
  formOpen.value = true;
  editing.value = row;
  form.value = { topic: row.topic, content: row.content };
  formMsg.value = '';
}

function closeForm() {
  formOpen.value = false;
  editing.value = null;
  formMsg.value = '';
}

async function doSave() {
  if (!form.value.topic.trim()) {
    formMsg.value = '请输入公告主题';
    return;
  }
  if (!form.value.content.trim()) {
    formMsg.value = '请输入公告内容';
    return;
  }
  formBusy.value = true;
  formMsg.value = '';
  try {
    const body = { topic: form.value.topic.trim(), content: form.value.content.trim() };
    const res = editing.value
      ? await updateNotice(editing.value.id, body)
      : await addNotice(body);
    if (res.code === 0) {
      closeForm();
      await load();
    } else {
      formMsg.value = res.mes || '保存失败';
    }
  } catch (e: any) {
    formMsg.value = e?.message || '保存失败';
  } finally {
    formBusy.value = false;
  }
}

const fmtDate = (d?: string | null) => (d ? String(d).replace('T', ' ').slice(0, 16) : '—');

async function load() {
  loading.value = true;
  error.value = '';
  try {
    const data = await listNotices({
      pageNum: pageNum.value,
      limit: limit.value,
      topic: topic.value || undefined,
      content: content.value || undefined,
    });
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

function onFilter() {
  pageNum.value = 1;
  load();
}

function onReset() {
  topic.value = '';
  content.value = '';
  pageNum.value = 1;
  load();
}

function onPrev() { if (pageNum.value > 1) { pageNum.value--; load(); } }
function onNext() { if (pageNum.value < totalPages.value) { pageNum.value++; load(); } }

function toggleSelect(id: number) {
  const i = selected.value.indexOf(id);
  if (i >= 0) selected.value.splice(i, 1);
  else selected.value.push(id);
}

function toggleAll() {
  if (selected.value.length === list.value.length) {
    selected.value = [];
  } else {
    selected.value = list.value.map((r) => r.id);
  }
}

async function doDelete() {
  if (!selected.value.length) {
    delMsg.value = '请选择要删除的公告';
    return;
  }
  delBusy.value = true;
  delMsg.value = '';
  try {
    const res = await deleteNotices(selected.value);
    if (res.code === 0) {
      delMsg.value = '删除成功';
      selected.value = [];
      await load();
    } else {
      delMsg.value = res.mes || '删除失败';
    }
  } catch (e: any) {
    delMsg.value = e?.message || '删除失败';
  } finally {
    delBusy.value = false;
  }
}

onMounted(load);
</script>

<template>
  <div class="notices">
    <h1>公告管理</h1>

    <div class="toolbar">
      <button class="btn" @click="openAdd">+ 发布公告</button>
    </div>

    <!-- 筛选 -->
    <div class="card">
      <div class="filter-row">
        <input v-model="topic" type="text" placeholder="主题（模糊）" class="input" />
        <input v-model="content" type="text" placeholder="内容（模糊）" class="input" />
        <button class="btn" :disabled="loading" @click="onFilter">
          {{ loading ? '查询中…' : '查询' }}
        </button>
        <button class="btn-ghost" :disabled="loading" @click="onReset">重置</button>
      </div>
    </div>

    <!-- 列表 -->
    <div class="card">
      <div v-if="selected.length" class="del-bar">
        <span>已选 {{ selected.length }} 条</span>
        <button class="btn-danger" :disabled="delBusy" @click="doDelete">
          {{ delBusy ? '删除中…' : '批量删除' }}
        </button>
        <p class="msg" :class="delMsg.includes('成功') ? 'ok' : 'err'">{{ delMsg }}</p>
      </div>

      <p v-if="loading" class="hint">加载中…</p>
      <p v-else-if="error" class="error">{{ error }}</p>
      <template v-else>
        <table v-if="list.length" class="table">
          <thead>
            <tr>
              <th class="col-check">
                <input type="checkbox" :checked="selected.length === list.length && list.length > 0" @change="toggleAll" />
              </th>
              <th>ID</th>
              <th>主题</th>
              <th>内容</th>
              <th>发布人</th>
              <th>发布时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in list" :key="r.id">
              <td class="col-check">
                <input type="checkbox" :checked="selected.includes(r.id)" @change="toggleSelect(r.id)" />
              </td>
              <td>{{ r.id }}</td>
              <td class="col-topic">{{ r.topic }}</td>
              <td class="col-content">{{ r.content }}</td>
              <td>{{ r.author || '—' }}</td>
              <td>{{ fmtDate(r.createDate) }}</td>
              <td>
                <button class="btn-small" @click="openEdit(r)">编辑</button>
              </td>
            </tr>
          </tbody>
        </table>
        <p v-else class="hint">暂无公告数据</p>
      </template>

      <!-- 分页 -->
      <div class="pager">
        <button class="btn-ghost" :disabled="pageNum <= 1 || loading" @click="onPrev">上一页</button>
        <span class="hint">第 {{ pageNum }} / {{ totalPages }} 页 · 共 {{ count }} 条</span>
        <button class="btn-ghost" :disabled="pageNum >= totalPages || loading" @click="onNext">下一页</button>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div v-if="formOpen" class="modal-mask" @click.self="closeForm">
      <div class="modal">
        <h3>{{ editing ? '编辑公告' : '发布公告' }}</h3>
        <p v-if="formMsg" class="msg err">{{ formMsg }}</p>
        <div class="field">
          <label>主题 *</label>
          <input v-model="form.topic" class="input" maxlength="50" placeholder="请输入公告主题" />
        </div>
        <div class="field">
          <label>内容 *</label>
          <textarea v-model="form.content" class="input" rows="5" maxlength="255" placeholder="请输入公告内容"></textarea>
        </div>
        <div class="modal-actions">
          <button class="btn-ghost" :disabled="formBusy" @click="closeForm">取消</button>
          <button class="btn" :disabled="formBusy" @click="doSave">
            {{ formBusy ? '保存中…' : '保存' }}
          </button>
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
.toolbar {
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
textarea.input {
  resize: vertical;
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
}
.btn-ghost:hover:not(:disabled) {
  background: #e2e8f0;
}
.btn-ghost:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.btn-danger {
  padding: 6px 16px;
  background: #ef4444;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
}
.btn-danger:hover:not(:disabled) {
  background: #dc2626;
}
.btn-danger:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.del-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 6px;
  padding: 10px 14px;
  margin-bottom: 12px;
  color: #991b1b;
  font-size: 14px;
}
.msg {
  font-size: 13px;
  margin: 8px 0 0;
}
.msg.ok {
  color: #10b981;
}
.msg.err {
  color: #ef4444;
}
.del-bar .msg {
  margin: 0;
  color: #b45309;
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
.col-check {
  width: 36px;
}
.col-topic {
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.col-content {
  max-width: 260px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
  margin: 0 0 12px;
  font-size: 16px;
  color: #1e293b;
}
.field {
  margin-bottom: 14px;
}
.field label {
  display: block;
  margin-bottom: 6px;
  color: #64748b;
  font-size: 14px;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
}
</style>
