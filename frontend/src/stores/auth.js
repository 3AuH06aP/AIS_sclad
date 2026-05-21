import { defineStore } from 'pinia';
import { login as apiLogin } from '../api/auth';
export const useAuthStore = defineStore('auth', {
    state: () => ({
        user: '',
        fullName: '',
        role: 'user',
        isAuthenticated: false,
        lastLoginAt: null
    }),
    actions: {
        async login(username, password) {
            if (!username || !password) {
                throw new Error('Введите логин и пароль');
            }
            const response = await apiLogin(username, password);
            this.user = response.username;
            this.fullName = (response.fullName && response.fullName.trim()) || '';
            this.role = response.role;
            this.lastLoginAt = response.lastLoginAt ?? null;
            this.isAuthenticated = true;
            localStorage.setItem('ais-stock-auth', JSON.stringify(response));
        },
        logout() {
            this.user = '';
            this.fullName = '';
            this.role = 'user';
            this.lastLoginAt = null;
            this.isAuthenticated = false;
            localStorage.removeItem('ais-stock-auth');
        },
        restore() {
            const raw = localStorage.getItem('ais-stock-auth');
            if (!raw) {
                return;
            }
            try {
                const stored = JSON.parse(raw);
                if (stored.username && (stored.token || stored.username === 'admin')) {
                    this.user = stored.username;
                    this.fullName = (stored.fullName && String(stored.fullName).trim()) || '';
                    this.role = (stored.role || 'user').toLowerCase();
                    this.lastLoginAt = stored.lastLoginAt ?? null;
                    this.isAuthenticated = true;
                }
            }
            catch {
                localStorage.removeItem('ais-stock-auth');
            }
        }
    }
});
