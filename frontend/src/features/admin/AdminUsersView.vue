<template>
  <div class="page">
    <main-layout>
      <template #title>Управление пользователями</template>
      <template #content>
        <section class="user-management">
          <div class="panel-card">
            <h2>Список пользователей</h2>
            <table>
              <thead>
                <tr>
                  <th>Имя</th>
                  <th>Роль</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="user in users" :key="user.id">
                  <td>{{ user.username }}</td>
                  <td>{{ user.role }}</td>
                </tr>
              </tbody>
            </table>
            <div v-if="users.length === 0" class="empty-state">Пользователи не найдены.</div>
          </div>

          <div class="panel-card create-user-card">
            <h3>Создать нового пользователя</h3>
            <form @submit.prevent="submit">
              <label>
                Логин
                <input v-model="username" placeholder="Имя пользователя" />
              </label>
              <label>
                Пароль
                <input v-model="password" type="password" placeholder="Пароль" />
              </label>
              <label>
                Роль
                <select v-model="role">
                  <option value="user">User</option>
                  <option value="admin">Admin</option>
                </select>
              </label>
              <button type="submit">Создать</button>
            </form>
            <p v-if="message" class="success">{{ message }}</p>
            <p v-if="error" class="error">{{ error }}</p>
          </div>
        </section>
      </template>
    </main-layout>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import MainLayout from '../../layouts/MainLayout.vue';
import { fetchUsers, createUser } from '../../api/users';
import type { UserRole } from '../../types';

const users = ref([] as Array<{ id: number; username: string; role: UserRole }>);
const username = ref('');
const password = ref('');
const role = ref<UserRole>('user');
const error = ref('');
const message = ref('');

async function loadUsers() {
  try {
    users.value = await fetchUsers();
  } catch {
    error.value = 'Не удалось загрузить список пользователей.';
  }
}

async function submit() {
  error.value = '';
  message.value = '';
  if (!username.value.trim() || !password.value.trim()) {
    error.value = 'Введите логин и пароль.';
    return;
  }

  try {
    await createUser({ username: username.value.trim(), password: password.value.trim(), role: role.value });
    message.value = 'Пользователь успешно создан.';
    username.value = '';
    password.value = '';
    role.value = 'user';
    await loadUsers();
  } catch {
    error.value = 'Ошибка при создании пользователя.';
  }
}

onMounted(loadUsers);
</script>

<style scoped>
.user-management {
  display: grid;
  gap: 20px;
}
.panel-card {
  background: #ffffff;
  border-radius: 18px;
  padding: 24px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.06);
}
table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 14px;
}
th,
td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e2e8f0;
}
label {
  display: block;
  margin-bottom: 14px;
}
input,
select {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  margin-top: 8px;
}
button {
  padding: 12px 18px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
}
.empty-state,
.success,
.error {
  margin-top: 16px;
  color: #475569;
}
.success {
  color: #16a34a;
}
.error {
  color: #b91c1c;
}
</style>
