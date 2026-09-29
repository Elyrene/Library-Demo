import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

const STORAGE_KEY = 'library_key';

export const useUserStore = defineStore('user', () => {
  const user = ref<readerInfo | adminInfo | null>(null);
  const cookie = ref<string>('');

  const isLoginedIn = computed(() => !!user.value);

  const persist = (data: readerInfo | adminInfo | null) => {
    if (data) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
    } else {
      localStorage.removeItem(STORAGE_KEY);
    }
  }

  const setAdmin = (payload: adminInfo) => {
    const normalized: adminInfo = {
      id: payload.id,
      username: payload.username,
      adminType: payload.adminType,
      cookie: payload.cookie,
    }
    user.value = normalized;
    cookie.value = normalized.cookie || '';
    persist(normalized);
  }

  const setReader = (payload: readerInfo) => {
    const normalized: readerInfo = {
      id: payload.id,
      username: payload.username,
      realName: payload.realName,
      sex: payload.sex,
      birthday: payload.birthday,
      address: payload.address,
      tel: payload.tel,
      email: payload.email,
      registerDate: payload.registerDate,
      readerNumber: payload.readerNumber,
      cookie: payload.cookie || cookie.value,
    }
    user.value = normalized;
    cookie.value = normalized.cookie || '';
    persist(normalized);
  }

  const setCookie = (val: string) => {
    if (!val) return;
    cookie.value = val;
    if (user.value) {
      user.value.cookie = val;
      persist(user.value);
    } else {
      localStorage.setItem(STORAGE_KEY + '_cookie', JSON.stringify(val));
    }
  }

  const clearUser = () => {
    user.value = null;
    cookie.value = '';
    persist(null);
    localStorage.removeItem(STORAGE_KEY + '_cookie');
  }

  const initFormlocal = () => {

    const rawCookie = localStorage.getItem(STORAGE_KEY + '_cookie');
    if (rawCookie) {
      try {
        cookie.value = JSON.parse(rawCookie);
      } catch (err) {
        console.log("本地初始化 token 失败", err);
      }
    }

    const raw = localStorage.getItem(STORAGE_KEY);
    if(!raw) return;

    try {
      const parsed: readerInfo | adminInfo = JSON.parse(raw);
      if (parsed && parsed.id) {
        user.value = parsed;
        cookie.value = parsed.cookie || cookie.value;
      } else {
        persist(null);
      }
    } catch (err) {
      console.log("初始化本地存储失败", err);
      persist(null);
    }

  }

  initFormlocal();

  return {
    user,
    cookie,
    isLoginedIn,
    setAdmin,
    setReader,
    setCookie,
    clearUser,
  }

})
