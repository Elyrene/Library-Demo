<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { api } from '@/api/index';

const loading = ref(false);
const error = ref('');
const list = ref<adminInfo[]>([]);

async function load() {
  loading.value = true;
  error.value = '';
  try {
    const data = await api.get<AdminsRes>('/api/admins');
    list.value = data.data;
  } catch (e: any) {
    error.value = e?.message || '加载失败';
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  load();
});
</script>

<template>
  <div class="admins">
    <h1>管理员管理</h1>
    <p v-if="loading" class="hint">加载中…</p>
    <p v-else-if="error" class="error">{{ error }}</p>
    <div v-else class="card">
      <table v-if="list.length" class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>角色</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in list" :key="a.id">
            <td>{{ a.id }}</td>
            <td>{{ a.username }}</td>
            <td>{{ a.adminType === 'librarian' ? '图书管理员' : a.adminType === 'manager' ? '馆长' : a.adminType }}</td>
          </tr>
        </tbody>
      </table>
      <p v-else class="hint">暂无数据</p>
    </div>
  </div>
</template>

<style scoped>
.admins h1 {
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
}
.card {
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
  overflow-x: auto;
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
</style>
