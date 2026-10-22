<script setup lang="ts">
import { ref } from 'vue';
import { useUserStore } from '@/stores/user';
import { api } from '@/api/index';

const userStore = useUserStore();
const newPassword = ref('');
const confirmPassword = ref('');
const loading = ref(false);
const error = ref('');
const success = ref('');

async function doChange() {
  error.value = '';
  success.value = '';
  if (!newPassword.value) {
    error.value = '请输入新密码';
    return;
  }
  if (newPassword.value !== confirmPassword.value) {
    error.value = '两次输入的密码不一致';
    return;
  }
  const id = (userStore.user as adminInfo | null)?.id;
  if (!id) {
    error.value = '用户信息不完整';
    return;
  }
  loading.value = true;
  try {
    await api.put<AdminsPasswordRes>(`/api/admins/${id}/password`, {
      password: newPassword.value,
    });
    success.value = '密码修改成功';
    newPassword.value = '';
    confirmPassword.value = '';
  } catch (e: any) {
    error.value = e?.message || '修改失败';
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="changepwd">
    <h1>修改密码</h1>
    <p v-if="success" class="success">{{ success }}</p>
    <p v-if="error" class="error">{{ error }}</p>
    <div class="form">
      <div class="field">
        <label>新密码</label>
        <input
          v-model="newPassword"
          type="password"
          placeholder="请输入新密码"
          class="input"
        />
      </div>
      <div class="field">
        <label>确认新密码</label>
        <input
          v-model="confirmPassword"
          type="password"
          placeholder="请再次输入新密码"
          class="input"
        />
      </div>
      <button class="btn" :disabled="loading" @click="doChange">
        {{ loading ? '提交中…' : '确认修改' }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.changepwd h1 {
  font-size: 20px;
  color: #1e293b;
  margin-bottom: 12px;
}
.success {
  color: #10b981;
  font-size: 14px;
  margin-bottom: 8px;
}
.error {
  color: #ef4444;
  font-size: 14px;
  margin-bottom: 8px;
}
.form {
  max-width: 400px;
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
}
.field {
  margin-bottom: 16px;
}
.field label {
  display: block;
  margin-bottom: 6px;
  color: #64748b;
  font-size: 14px;
}
.input {
  width: 100%;
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
  width: 100%;
  padding: 10px 0;
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
</style>
