<script setup lang="ts">
import { ref } from 'vue';
import { api } from '@/api/index';

const queryId = ref('');
const loading = ref(false);
const error = ref('');
const result = ref<readerInfo | null>(null);

async function doQuery() {
  if (!queryId.value.trim()) {
    error.value = '请输入读者编号';
    return;
  }
  loading.value = true;
  error.value = '';
  result.value = null;
  try {
    const data = await api.get<ReadersIdRes>(`/api/readers/${queryId.value.trim()}`);
    result.value = data.data;
  } catch (e: any) {
    error.value = e?.message || '查询失败';
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="readers">
    <h1>读者管理</h1>
    <div class="search-bar">
      <input
        v-model="queryId"
        type="text"
        placeholder="请输入读者编号"
        class="input"
      />
      <button class="btn" :disabled="loading" @click="doQuery">
        {{ loading ? '查询中…' : '查询' }}
      </button>
    </div>
    <p v-if="error" class="error">{{ error }}</p>
    <div v-else-if="result" class="card">
      <div class="row">
        <span class="label">读者编号</span>
        <span class="value">{{ result.id }}</span>
      </div>
      <div class="row">
        <span class="label">用户名</span>
        <span class="value">{{ result.username }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.readers h1 {
  font-size: 20px;
  color: #1e293b;
  margin-bottom: 12px;
}
.search-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}
.input:focus {
  border-color: #10b981;
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
}
.btn:hover:not(:disabled) {
  background: #059669;
}
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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
  max-width: 400px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
}
.row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f1f5f9;
}
.row:last-child {
  border-bottom: none;
}
.label {
  color: #64748b;
  font-size: 14px;
}
.value {
  color: #1e293b;
  font-size: 14px;
  font-weight: 500;
}
</style>
