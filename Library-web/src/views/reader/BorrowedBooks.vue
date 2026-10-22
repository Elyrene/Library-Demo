<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useUserStore } from '@/stores/user';
import { listLendsByReader, lendBook, backLend } from '@/api/lends';

const userStore = useUserStore();
const readerId = computed(() => (userStore.user as readerInfo | null)?.id);

const loading = ref(false);
const error = ref('');
const list = ref<lendInfo[]>([]);

// 借书表单
const formBookId = ref<number | null>(null);
const lendMsg = ref('');
const lendBusy = ref(false);

// 还书弹窗
const backTarget = ref<lendInfo | null>(null);
const backType = ref<number>(1);
const backRemarks = ref('');
const backBusy = ref(false);
const backMsg = ref('');

const fmt = (d?: string | null) => (d ? String(d).replace('T', ' ').slice(0, 16) : '—');
const backTypeLabel = (t?: number | null) =>
  t == null ? '未归还' : t === 1 ? '按时归还' : t === 2 ? '逾期归还' : String(t);

async function load() {
  const id = readerId.value;
  if (!id) {
    error.value = '用户信息不完整';
    return;
  }
  loading.value = true;
  error.value = '';
  try {
    const data = await listLendsByReader(id);
    if (data.code === 0) {
      list.value = data.data || [];
    } else {
      error.value = data.mes || '加载失败';
    }
  } catch (e: any) {
    error.value = e?.message || '加载失败';
  } finally {
    loading.value = false;
  }
}

async function doLend() {
  const id = readerId.value;
  if (!id) {
    lendMsg.value = '用户信息不完整';
    return;
  }
  if (formBookId.value == null || formBookId.value <= 0) {
    lendMsg.value = '请输入图书 ID';
    return;
  }
  lendBusy.value = true;
  lendMsg.value = '';
  try {
    const res = await lendBook(formBookId.value, id);
    if (res.code === 0) {
      lendMsg.value = '借书成功';
      formBookId.value = null;
      await load();
    } else {
      lendMsg.value = res.mes || '借书失败';
    }
  } catch (e: any) {
    lendMsg.value = e?.message || '借书失败';
  } finally {
    lendBusy.value = false;
  }
}

function openBack(row: lendInfo) {
  backTarget.value = row;
  backType.value = 1;
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
      backType: backType.value,
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

onMounted(load);
</script>

<template>
  <div class="borrowed">
    <h1>我的借阅</h1>
    <p v-if="loading" class="hint">加载中…</p>
    <p v-else-if="error" class="error">{{ error }}</p>

    <!-- 借书 -->
    <div class="card lend-card">
      <div class="lend-title">借书</div>
      <div class="lend-row">
        <input
          v-model.number="formBookId"
          type="number"
          min="1"
          placeholder="请输入图书 ID"
          class="input"
        />
        <button class="btn" :disabled="lendBusy" @click="doLend">
          {{ lendBusy ? '借出中…' : '借出' }}
        </button>
      </div>
      <p class="msg" :class="lendMsg.includes('成功') ? 'ok' : 'err'">{{ lendMsg }}</p>
    </div>

    <!-- 借阅列表 -->
    <div class="card">
      <table v-if="list.length" class="table">
        <thead>
          <tr>
            <th>图书</th>
            <th>作者</th>
            <th>借出日期</th>
            <th>状态</th>
            <th>归还日期</th>
            <th>备注</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in list" :key="r.id">
            <td>{{ r.bookName || '—' }}</td>
            <td>{{ r.bookAuthor || '—' }}</td>
            <td>{{ fmt(r.lendDate) }}</td>
            <td>
              <span class="tag" :class="r.backDate ? 'backed' : 'open'">
                {{ r.backDate ? '已归还' : '借出中' }}
              </span>
            </td>
            <td>{{ fmt(r.backDate) }}</td>
            <td>{{ r.exceptRemarks || '—' }}</td>
            <td>
              <button
                v-if="!r.backDate"
                class="btn-small"
                @click="openBack(r)"
              >
                还书
              </button>
              <span v-else class="hint">—</span>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="hint">暂无借阅记录</p>
    </div>

    <!-- 还书弹窗 -->
    <div v-if="backTarget" class="modal-mask" @click.self="backTarget = null">
      <div class="modal">
        <h3>还书 — {{ backTarget.bookName || backTarget.id }}</h3>
        <p v-if="backMsg" class="msg err">{{ backMsg }}</p>
        <div class="field">
          <label>归还类型</label>
          <select v-model.number="backType" class="input">
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
.borrowed h1 {
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
  max-width: 480px;
}
.lend-title {
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  margin-bottom: 10px;
}
.lend-row {
  display: flex;
  gap: 8px;
}
.input {
  flex: 1;
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
}
.btn-ghost:hover {
  background: #e2e8f0;
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
