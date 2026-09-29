import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

const STORAGE_KEY = 'library_key';


export const useUserStore = defineStore('user', () => {
  const user = ref<readerInfo | adminInfo | null>(null);
  const cookie = ref<string>('');
  const role = ref<string>('');

  const isLoggedIn = computed(() => !!user.value);

  const persist = () => {
    if (user.value || role.value) {
      const data: SessionData = { user: user.value, role: role.value, cookie: cookie.value };
      localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
    } else {
      localStorage.removeItem(STORAGE_KEY);
    }
  }

  const setAdmin = (payload: adminInfo, roleVal?: string) => {
    user.value = {
      id: payload.id,
      username: payload.username,
      adminType: payload.adminType,
      cookie: payload.cookie || cookie.value,
    }
    cookie.value = (user.value as adminInfo).cookie || '';
    if(roleVal) role.value = roleVal;
    persist();
  }

  const setReader = (payload: readerInfo) => {
    user.value = {
      ...payload,
      cookie: payload.cookie || cookie.value,
    }
    cookie.value = (user.value as readerInfo).cookie || '';
    persist();
  }

  const setRole = (val: string) => {
    if (!val) return;
    role.value = val;
    persist();
  }

  const setCookie = (val: string) => {
    if (!val) return;
    cookie.value = val;
    if (user.value) {
      (user.value as readerInfo | adminInfo).cookie = val;
    } 
    persist();
  }

  const clearUser = () => {
    user.value = null;
    role.value = '';
    cookie.value = '';
    localStorage.removeItem(STORAGE_KEY);
  }

  const initFromlocal = () => {
    // SSR 防御
    if(typeof window === 'undefined') return;

    const raw = localStorage.getItem(STORAGE_KEY);
    if(!raw) return;

    try {
      const parsed: SessionData = JSON.parse(raw);
      if (parsed?.user?.id && parsed.user.id !== null) {
        const roleOk = (parsed.role === 'admin' && 'adminType' in parsed.user)
          || (parsed.role === 'reader' && 'readerNumber' in parsed.user);
        if (!roleOk) { localStorage.removeItem(STORAGE_KEY); return; }
        user.value = parsed.user;
        role.value = parsed.role || ''; 
        cookie.value = parsed.cookie || '';
      } else {
        localStorage.removeItem(STORAGE_KEY);
      }
    } catch (err) {
      console.log("初始化本地存储失败", err);
      localStorage.removeItem(STORAGE_KEY);
    }

  }

  initFromlocal();

  return {
    user,
    role,
    cookie,
    isLoggedIn,
    setRole,
    setAdmin,
    setReader,
    setCookie,
    clearUser,
  }

})
