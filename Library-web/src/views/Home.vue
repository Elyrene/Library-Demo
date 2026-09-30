<script setup lang="ts">
import { api } from '@/api';
import { useUserStore } from '@/stores/user';
import { useRouter } from 'vue-router';

const router = useRouter();
const UserStore = useUserStore();

const logout = async () => {
    try {
        const data = await api.post<AuthLogoutRes>('/api/auth/logout', {});
        console.log(data);
        if (data.code == 0) {
            UserStore.clearUser();
            router.push('/login');
        } 
    } catch(err) {
        console.log("登出失败",err);
    }
}

</script>
    
<template>
    <button @click="logout">退出登录</button>
</template>

<style scoped></style>
