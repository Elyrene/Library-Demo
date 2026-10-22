<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useUserStore } from '@/stores/user';
import { api } from '@/api/index';

const userStore = useUserStore();
const loading = ref(false);
const error = ref('');
const result = ref<readerInfo | null>(null);

async function doQuery() {
  const id = userStore.user?.id;
  if (!id) {
    error.value = '用户信息不完整';
    return;
  }
  loading.value = true;
  error.value = '';
  try {
    const data = await api.get<ReadersIdRes>(`/api/readers/${id}`);
    result.value = data.data;
  } catch (e: any) {
    error.value = e?.message || '查询失败';
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  doQuery();
});
</script>

<template>
  <div class="profile">
    <h1>个人中心</h1>
    <p v-if="loading" class="hint">加载中…</p>
    <p v-else-if="error" class="error">{{ error }}</p>
    <div v-else-if="result" class="info-card">
      <div class="row">
        <span class="label">读者编号</span>
        <span class="value">{{ result.id }}</span>
      </div>
      <div class="row">
        <span class="label">用户名</span>
        <span class="value">{{ result.username }}</span>
      </div>
    </div>
    <p v-else class="hint">暂无数据</p>
  </div>
</template>

<style scoped>
.profile h1 {
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
.info-card {
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
