import { api } from '@/api';
import { useUserStore } from '@/stores/user';
import { useRouter } from 'vue-router';

export function useLogout() {
  const router = useRouter();
  const UserStore = useUserStore();

  const logout = async () => {
    try {
      await api.post<AuthLogoutRes>('/api/auth/logout', {});
      UserStore.clearUser();
      router.push('/login');
    } catch (err) {
      console.log('登出失败', err);
    }
  };

  return { logout };
}
