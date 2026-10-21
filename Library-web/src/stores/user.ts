import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

const STORAGE_KEY = 'library_key';


export const useUserStore = defineStore('user', () => {
  const user = ref<readerInfo | adminInfo | null>(null);
  const cookie = ref<string>('');
  const role = ref<string>('');
  const rememberMe = ref<boolean>(false);

  const setRememberMe = (val: boolean) => {
    rememberMe.value = val;
    // 如果已有会话数据，按新标志重新落盘
    persist();
  }

  const isLoggedIn = computed(() => !!user.value);

  const persist = () => {
    if (user.value || role.value) {
      const data: SessionData = { user: user.value, role: role.value, cookie: cookie.value };
      const store = rememberMe.value ? localStorage : sessionStorage;
      store.setItem(STORAGE_KEY, JSON.stringify(data));
      (rememberMe.value ? sessionStorage : localStorage).removeItem(STORAGE_KEY);
    } else {
      localStorage.removeItem(STORAGE_KEY);
      sessionStorage.removeItem(STORAGE_KEY);
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
    rememberMe.value = false;
    localStorage.removeItem(STORAGE_KEY);
    sessionStorage.removeItem(STORAGE_KEY);
  }

  const initFromlocal = () => {
    // SSR 防御
    if(typeof window === 'undefined') return;

    const readFrom = (storage: Storage): boolean => {
      const raw = storage.getItem(STORAGE_KEY);
      if (!raw) return false;
      try {
        const parsed: SessionData = JSON.parse(raw);
        if (parsed?.user?.id && parsed.user.id !== null) {
          const roleOk = (parsed.role === 'admin' && 'adminType' in parsed.user)
            || (parsed.role === 'reader' && 'readerNumber' in parsed.user);
          if (!roleOk) { storage.removeItem(STORAGE_KEY); return false; }
          user.value = parsed.user;
          role.value = parsed.role || '';
          cookie.value = parsed.cookie || '';
          return true;
        } else {
          storage.removeItem(STORAGE_KEY);
        }
      } catch (err) {
        console.log("初始化本地存储失败", err);
        storage.removeItem(STORAGE_KEY);
      }
      return false;
    };

    rememberMe.value = readFrom(localStorage);
    if (!rememberMe.value) readFrom(sessionStorage);
  }

  initFromlocal();

  return {
    user,
    role,
    cookie,
    rememberMe,
    isLoggedIn,
    setRole,
    setAdmin,
    setReader,
    setCookie,
    setRememberMe,
    clearUser,
  }
})
