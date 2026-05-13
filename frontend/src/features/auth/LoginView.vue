<template>
  <div class="page auth-page">
    <div class="auth-card">
      <h1>Вход в AIS Stock</h1>
      <form @submit.prevent="submit">
        <label>
          Логин
          <input v-model="username" placeholder="admin или user" />
        </label>
        <label>
          Пароль
          <input v-model="password" type="password" placeholder="admin или user" />
        </label>
        <button type="submit">Войти</button>
      </form>
      <p class="hint">Для доступа используйте admin/admin или любой другой логин + password.</p>
      <p class="hint">Администратор попадёт на `/admin`, обычный пользователь — на главную панель.</p>
      <p v-if="error" class="error">{{ error }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../../stores/auth';

const router = useRouter();
const auth = useAuthStore();
const username = ref('');
const password = ref('');
const error = ref('');

async function submit() {
  try {
    await auth.login(username.value.trim(), password.value.trim());
    router.push(auth.role === 'admin' ? '/admin' : '/');
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Ошибка входа';
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24px;
}
.auth-card {
  width: 100%;
  max-width: 420px;
  padding: 32px;
  border-radius: 18px;
  background: #ffffff;
  box-shadow: 0 16px 48px rgba(15, 23, 42, 0.08);
}
h1 {
  margin-bottom: 24px;
  font-size: 1.6rem;
}
label {
  display: block;
  margin-bottom: 16px;
  font-weight: 600;
}
input {
  width: 100%;
  padding: 12px 14px;
  margin-top: 8px;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  font-size: 0.95rem;
}
button {
  width: 100%;
  padding: 12px 14px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 700;
}
button:hover {
  background: #1d4ed8;
}
.hint {
  margin-top: 16px;
  color: #475569;
}
.error {
  margin-top: 16px;
  color: #b91c1c;
}
</style>
