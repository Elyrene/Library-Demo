<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { listBooks, addBook, updateBook, deleteBooks } from '@/api/books';

const loading = ref(false);
const error = ref('');
const list = ref<bookInfo[]>([]);
const count = ref(0);
const pageNum = ref(1);
const limit = ref(10);
const totalPages = computed(() => Math.max(1, Math.ceil(count.value / limit.value)));

// 筛选
const name = ref('');
const typeId = ref<number | null>(null);
const status = ref<number | null>(null);

// 新增/编辑弹窗
const formOpen = ref(false);
const editing = ref<bookInfo | null>(null);
const form = ref({
  name: '',
  author: '',
  publish: '',
  isbn: '',
  introduction: '',
  language: '',
  price: 0,
  publishDate: '',
  typeId: 0,
});
const formBusy = ref(false);
const formMsg = ref('');

// 批量删除
const selected = ref<number[]>([]);
const delMsg = ref('');
const delBusy = ref(false);

function openAdd() {
  formOpen.value = true;
  editing.value = null;
  form.value = { name: '', author: '', publish: '', isbn: '', introduction: '', language: '', price: 0, publishDate: '', typeId: 0 };
  formMsg.value = '';
}

function openEdit(row: bookInfo) {
  formOpen.value = true;
  editing.value = row;

  form.value = {
    name: row.name,
    author: row.author,
    publish: row.publish,
    isbn: row.isbn,
    introduction: row.introduction,
    language: row.language,
    price: row.price,
    publishDate: row.publishDate,
    typeId: row.typeId,
  };
  formMsg.value = '';
}

function closeForm() {
  formOpen.value = false;
  editing.value = null;
  formMsg.value = '';
}

async function doSave() {
  if (!form.value.name.trim()) {
    formMsg.value = '请输入书名';
    return;
  }
  if (!form.value.author.trim()) {
    formMsg.value = '请输入作者';
    return;
  }
  formBusy.value = true;
  formMsg.value = '';
  try {
    const body = { ...form.value };
    let res;
    if (editing.value) {
      res = await updateBook(editing.value.id, body);
    } else {
      res = await addBook(body);
    }
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
      status: status.value ?? undefined,
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

function onFilter() {
  pageNum.value = 1;
  load();
}
function onReset() {
  name.value = '';
  typeId.value = null;
  status.value = null;
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
    delMsg.value = '请选择要删除的图书';
    return;
  }
  delBusy.value = true;
  delMsg.value = '';
  try {
    const res = await deleteBooks(selected.value);
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
  <div class="books">
    <h1>图书管理</h1>

    <!-- 新增按钮 -->
    <div class="toolbar">
      <button class="btn" @click="openAdd">+ 新增图书</button>
    </div>

    <!-- 筛选 -->
    <div class="card">
      <div class="filter-row">
        <input v-model="name" type="text" placeholder="书名（模糊）" class="input" />
        <input v-model.number="typeId" type="number" min="0" placeholder="分类 ID" class="input narrow" />
        <select v-model.number="status" class="input sel">
          <option :value="null">全部状态</option>
          <option :value="0">未借出</option>
          <option :value="1">已借出</option>
        </select>
        <button class="btn" :disabled="loading" @click="onFilter">
          {{ loading ? '查询中…' : '查询' }}
        </button>
        <button class="btn-ghost" :disabled="loading" @click="onReset">重置</button>
      </div>
    </div>

    <!-- 列表 -->
    <div class="card">
      <div v-if="selected.length" class="del-bar">
        <span>已选 {{ selected.length }} 本</span>
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
              <th>书名</th>
              <th>作者</th>
              <th>出版社</th>
              <th>ISBN</th>
              <th>价格</th>
              <th>出版日期</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in list" :key="r.id">
              <td class="col-check">
                <input type="checkbox" :checked="selected.includes(r.id)" @change="toggleSelect(r.id)" />
              </td>
              <td>{{ r.id }}</td>
              <td class="col-name">{{ r.name }}</td>
              <td>{{ r.author }}</td>
              <td>{{ r.publish }}</td>
              <td>{{ r.isbn || '—' }}</td>
              <td>{{ r.price != null ? '¥' + r.price.toFixed(2) : '—' }}</td>
              <td>{{ fmtDate(r.publishDate) }}</td>
              <td>
                <span class="tag" :class="r.status === 0 ? 'open' : 'backed'">
                  {{ r.status === 0 ? '未借出' : '已借出' }}
                </span>
              </td>
              <td>
                <button class="btn-small" @click="openEdit(r)">编辑</button>
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

    <!-- 新增/编辑弹窗 -->
    <div v-if="formOpen" class="modal-mask" @click.self="closeForm">
      <div class="modal modal-wide">
        <h3>{{ editing ? '编辑图书' : '新增图书' }}</h3>
        <p v-if="formMsg" class="msg err">{{ formMsg }}</p>
        <div class="field">
          <label>书名 *</label>
          <input v-model="form.name" class="input" maxlength="20" placeholder="请输入书名" />
        </div>
        <div class="field">
          <label>作者 *</label>
          <input v-model="form.author" class="input" maxlength="30" placeholder="请输入作者" />
        </div>
        <div class="field">
          <label>出版社</label>
          <input v-model="form.publish" class="input" maxlength="30" placeholder="请输入出版社" />
        </div>
        <div class="field">
          <label>ISBN</label>
          <input v-model="form.isbn" class="input" maxlength="30" placeholder="请输入 ISBN" />
        </div>
        <div class="field">
          <label>简介</label>
          <textarea v-model="form.introduction" class="input" rows="2" maxlength="50" placeholder="请输入图书简介（选填）"></textarea>
        </div>
        <div class="field-row">
          <div class="field">
            <label>语言</label>
            <input v-model="form.language" class="input" maxlength="20" placeholder="如：中文" />
          </div>
          <div class="field">
            <label>价格（¥）</label>
            <input v-model.number="form.price" type="number" min="0" step="0.01" class="input" placeholder="0.00" />
          </div>
        </div>
        <div class="field-row">
          <div class="field">
            <label>出版日期</label>
            <input v-model="form.publishDate" type="date" class="input" />
          </div>
          <div class="field">
            <label>分类 ID</label>
            <input v-model.number="form.typeId" type="number" min="0" class="input" placeholder="如：1" />
          </div>
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
.books h1 {
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
.narrow {
  flex: 0 0 auto;
  width: 120px;
}
.sel {
  flex: 0 0 auto;
  width: auto;
  min-width: 120px;
}
textarea.input {
  resize: vertical;
}
.field-row {
  display: flex;
  gap: 12px;
}
.field-row .field {
  flex: 1;
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
