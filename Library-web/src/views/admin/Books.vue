<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import {
  listLends,
  lendBook,
  backLend,
  deleteLends,
} from '@/api/lends';

const loading = ref(false);
const error = ref('');
const list = ref<lendInfo[]>([]);
const count = ref(0);
const pageNum = ref(1);
const limit = ref(10);
const totalPages = computed(() => Math.max(1, Math.ceil(count.value / limit.value)));

// 筛选
const bookName = ref('');
const readerName = ref('');
const backType = ref<number | null>(null);

// 借书表单
const formBookId = ref<number | null>(null);
const formReaderId = ref<number | null>(null);
const lendMsg = ref('');
const lendBusy = ref(false);

// 还书弹窗
const backTarget = ref<lendInfo | null>(null);
const backTypeSel = ref<number>(1);
const backRemarks = ref('');
const backBusy = ref(false);
const backMsg = ref('');

// 批量删除
const selected = ref<number[]>([]);
const delMsg = ref('');
const delBusy = ref(false);

const fmt = (d?: string | null) => (d ? String(d).replace('T', ' ').slice(0, 16) : '—');

async function load() {
  loading.value = true;
  error.value = '';
  try {
    const data = await listLends({
      pageNum: pageNum.value,
      limit: limit.value,
      bookName: bookName.value || undefined,
      readerName: readerName.value || undefined,
      backType: backType.value,
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
  bookName.value = '';
  readerName.value = '';
  backType.value = null;
  pageNum.value = 1;
  load();
}

function onPrev() {
  if (pageNum.value > 1) {
    pageNum.value--;
    load();
  }
}
function onNext() {
  if (pageNum.value < totalPages.value) {
    pageNum.value++;
    load();
  }
}

async function doLend() {
  if (formBookId.value == null || formBookId.value <= 0) {
    lendMsg.value = '请输入图书 ID';
    return;
  }
  if (formReaderId.value == null || formReaderId.value <= 0) {
    lendMsg.value = '请输入读者 ID';
    return;
  }
  lendBusy.value = true;
  lendMsg.value = '';
  try {
    const res = await lendBook(formBookId.value, formReaderId.value);
    if (res.code === 0) {
      lendMsg.value = '借出成功';
      formBookId.value = null;
      formReaderId.value = null;
      await load();
    } else {
      lendMsg.value = res.mes || '借出失败';
    }
  } catch (e: any) {
    lendMsg.value = e?.message || '借出失败';
  } finally {
    lendBusy.value = false;
  }
}

function openBack(row: lendInfo) {
  backTarget.value = row;
  backTypeSel.value = 1;
  backRemarks.value = '';
  backMsg.value = '';
}

async function doBack() {
  const target = backTarget.value;
  if (!target) return;
  backBusy.value = true;
  backMsg.value = '';
  try {
    const res = await backLend(target.id, {
      backType: backTypeSel.value,
      exceptRemarks: backRemarks.value || undefined,
    });
    if (res.code === 0) {
      backTarget.value = null;
      await load();
    } else {
      backMsg.value = res.mes || '还书失败';
    }
  } catch (e: any) {
    backMsg.value = e?.message || '还书失败';
  } finally {
    backBusy.value = false;
  }
}

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
    delMsg.value = '请选择要删除的借阅记录';
    return;
  }
  delBusy.value = true;
  delMsg.value = '';
  try {
    const res = await deleteLends(selected.value);
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
    <h1>借阅管理</h1>

    <!-- 借书 -->
    <div class="card lend-card">
      <div class="lend-title">借书</div>
      <div class="lend-row">
        <input v-model.number="formBookId" type="number" min="1" placeholder="图书 ID" class="input" />
        <input v-model.number="formReaderId" type="number" min="1" placeholder="读者 ID" class="input" />
        <button class="btn" :disabled="lendBusy" @click="doLend">
          {{ lendBusy ? '借出中…' : '借出' }}
        </button>
      </div>
      <p class="msg" :class="lendMsg.includes('成功') ? 'ok' : 'err'">{{ lendMsg }}</p>
    </div>

    <!-- 筛选 -->
    <div class="card">
      <div class="filter-row">
        <input v-model="bookName" type="text" placeholder="书名（模糊）" class="input" />
        <input v-model="readerName" type="text" placeholder="读者姓名（模糊）" class="input" />
        <select v-model.number="backType" class="input sel">
          <option :value="null">全部状态</option>
          <option :value="0">借出中</option>
          <option :value="1">按时归还</option>
          <option :value="2">逾期归还</option>
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
              <th>图书</th>
              <th>作者</th>
              <th>读者</th>
              <th>读者编号</th>
              <th>借出日期</th>
              <th>状态</th>
              <th>归还日期</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in list" :key="r.id">
              <td class="col-check">
                <input type="checkbox" :checked="selected.includes(r.id)" @change="toggleSelect(r.id)" />
              </td>
              <td>{{ r.id }}</td>
              <td>{{ r.bookName || '—' }}</td>
              <td>{{ r.bookAuthor || '—' }}</td>
              <td>{{ r.readerRealName || '—' }}</td>
              <td>{{ r.readerNumber || '—' }}</td>
              <td>{{ fmt(r.lendDate) }}</td>
              <td>
                <span class="tag" :class="r.backDate ? 'backed' : 'open'">
                  {{ r.backDate ? '已归还' : '借出中' }}
                </span>
              </td>
              <td>{{ fmt(r.backDate) }}</td>
              <td>
                <button v-if="!r.backDate" class="btn-small" @click="openBack(r)">还书</button>
                <span v-else class="hint">—</span>
              </td>
            </tr>
          </tbody>
        </table>
        <p v-else class="hint">暂无借阅记录</p>
      </template>

      <!-- 分页 -->
      <div class="pager">
        <button class="btn-ghost" :disabled="pageNum <= 1 || loading" @click="onPrev">上一页</button>
        <span class="hint">第 {{ pageNum }} / {{ totalPages }} 页 · 共 {{ count }} 条</span>
        <button class="btn-ghost" :disabled="pageNum >= totalPages || loading" @click="onNext">下一页</button>
      </div>
    </div>

    <!-- 还书弹窗 -->
    <div v-if="backTarget" class="modal-mask" @click.self="backTarget = null">
      <div class="modal">
        <h3>还书 — {{ backTarget.bookName || backTarget.id }}</h3>
        <p v-if="backMsg" class="msg err">{{ backMsg }}</p>
        <div class="field">
          <label>归还类型</label>
          <select v-model.number="backTypeSel" class="input">
            <option :value="1">按时归还</option>
            <option :value="2">逾期归还</option>
          </select>
        </div>
        <div class="field">
          <label>备注</label>
          <textarea v-model="backRemarks" class="input" rows="3" placeholder="选填"></textarea>
        </div>
        <div class="modal-actions">
          <button class="btn-ghost" :disabled="backBusy" @click="backTarget = null">取消</button>
          <button class="btn" :disabled="backBusy" @click="doBack">
            {{ backBusy ? '提交中…' : '确认还书' }}
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
.lend-card {
  max-width: 640px;
}
.lend-title {
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  margin-bottom: 10px;
}
.lend-row,
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
.sel {
  flex: 0 0 auto;
  width: auto;
  min-width: 120px;
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
