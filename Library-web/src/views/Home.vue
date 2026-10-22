<script setup lang="ts">
import { onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();

const redirectPath = computed(() => {
  if (userStore.role === 'admin') return '/admin/dashboard';
  if (userStore.role === 'reader') return '/reader/dashboard';
  return '/login';
});

onMounted(() => {
  router.replace(redirectPath.value);
});
</script>

<template>
  <div class="home-page">
    <p>跳转中…</p>
  </div>
</template>

<style scoped>
.home-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
}
p {
  color: #94a3b8;
  font-size: 14px;
}
</style>
