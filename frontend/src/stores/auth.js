import { defineStore } from 'pinia';
import { login as apiLogin } from '../api/auth';
export const useAuthStore = defineStore('auth', {
    state: () => ({
        user: '',
        role: 'user',
        isAuthenticated: false
    }),
    actions: {
        async login(username, password) {
            if (!username || !password) {
                throw new Error('Введите логин и пароль');
            }
            const response = await apiLogin(username, password);
            this.user = response.username;
            this.role = response.role;
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
                const stored = JSON.parse(raw);
                if (stored.username) {
                    this.user = stored.username;
                    this.role = stored.role;
                    this.isAuthenticated = true;
                }
            }
            catch {
                localStorage.removeItem('ais-stock-auth');
            }
        }
    }
});
//# sourceMappingURL=auth.js.map