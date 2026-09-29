<script setup lang="ts">
import { useUserStore } from '@/stores/user';
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const UserStore = useUserStore();

const logout = () => {
    UserStore.clearUser();
    router.push('/login');
}
</script>
    
<template>
    <button @click="logout">退出登录</button>
</template>

<style scoped></style>
