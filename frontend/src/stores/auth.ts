import { defineStore } from 'pinia';
import type { UserRole } from '../types';
import { login as apiLogin } from '../api/auth';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: '' as string,
    role: 'user' as UserRole,
    isAuthenticated: false as boolean
  }),
  actions: {
    async login(username: string, password: string) {
      if (!username || !password) {
        throw new Error('Введите логин и пароль');
      }
      const response = await apiLogin(username, password);
      this.user = response.username;
      this.role = response.role as UserRole;
      this.isAuthenticated = true;
      localStorage.setItem('ais-stock-auth', JSON.stringify(response));
    },
    logout() {
      this.user = '';
      this.role = 'user';
      this.isAuthenticated = false;
      localStorage.removeItem('ais-stock-auth');
    },
    restore() {
      const raw = localStorage.getItem('ais-stock-auth');
      if (!raw) {
        return;
      }
      try {
        const stored = JSON.parse(raw) as { username: string; role: UserRole };
        if (stored.username) {
          this.user = stored.username;
          this.role = stored.role;
          this.isAuthenticated = true;
        }
      } catch {
        localStorage.removeItem('ais-stock-auth');
      }
    }
  }
});
