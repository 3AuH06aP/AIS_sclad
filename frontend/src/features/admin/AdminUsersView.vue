<template>
  <div class="page">
    <main-layout>
      <template #title>Управление пользователями</template>
      <template #content>
        <section class="admin-users">
          <div class="table-card">
            <div class="card-header">
              <h2>Пользователи системы</h2>
              <button @click="showCreate = !showCreate" class="btn btn-primary">
                {{ showCreate ? 'Скрыть форму' : '+ Новый пользователь' }}
              </button>
            </div>

            <div v-if="showCreate" class="create-form panel-card">
              <h3>Создание аккаунта</h3>
              <form @submit.prevent="submit" class="form-grid">
                <div class="form-group">
                  <label>Логин</label>
                  <input v-model="username" placeholder="Имя пользователя" required />
                </div>
                <div class="form-group">
                  <label>Пароль</label>
                  <input v-model="password" type="password" placeholder="Минимум 4 символа" required />
                </div>
                <div class="form-group">
                  <label>Роль</label>
                  <select v-model="role">
                    <option value="user">Пользователь (Склад)</option>
                    <option value="admin">Администратор</option>
                  </select>
                </div>
                <div class="form-actions">
                  <button type="submit" class="btn btn-success">Создать</button>
                </div>
              </form>
              <p v-if="message" class="success-msg">{{ message }}</p>
              <p v-if="error" class="error-msg">{{ error }}</p>
            </div>

            <table class="table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Имя пользователя</th>
                  <th>Роль</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="user in users" :key="user.id">
                  <td>#{{ user.id }}</td>
                  <td class="user-name">👤 {{ user.username }}</td>
                  <td>
                    <span :class="['role-tag', user.role]">
                      {{ user.role === 'admin' ? 'Админ' : 'Склад' }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-if="users.length === 0" class="empty-state">Нет данных</div>
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

const users = ref([]);
const username = ref('');
const password = ref('');
const role = ref<UserRole>('user');
const error = ref('');
const message = ref('');
const showCreate = ref(false);

async function loadUsers() {
  try {
    users.value = await fetchUsers();
  } catch {
    error.value = 'Ошибка загрузки';
  }
}

async function submit() {
  error.value = '';
  message.value = '';
  try {
    await createUser({ username: username.value.trim(), password: password.value.trim(), role: role.value });
    message.value = 'Пользователь создан';
    username.value = '';
    password.value = '';
    showCreate.value = false;
    await loadUsers();
  } catch {
    error.value = 'Ошибка создания';
  }
}

onMounted(loadUsers);
</script>

<style scoped>
.table-card {
  background: white;
  border-radius: 18px;
  padding: 24px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.06);
}
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.card-header h2 { margin: 0; }

.create-form { background: #f8fafc; margin-bottom: 24px; border: 1px solid #e2e8f0; }
.form-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; align-items: flex-end; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
label { font-size: 0.9rem; font-weight: 600; color: #64748b; }
input, select { padding: 10px; border: 1px solid #cbd5e1; border-radius: 8px; }

.table { width: 100%; border-collapse: collapse; }
th { text-align: left; padding: 14px; color: #64748b; border-bottom: 1px solid #e2e8f0; }
td { padding: 14px; border-bottom: 1px solid #f1f5f9; }
.user-name { font-weight: 600; color: #0f172a; }

.role-tag { padding: 4px 10px; border-radius: 6px; font-size: 0.8rem; font-weight: 700; }
.role-tag.admin { background: #fee2e2; color: #ef4444; }
.role-tag.user { background: #e0f2fe; color: #0369a1; }

.btn { padding: 10px 20px; border-radius: 10px; border: none; cursor: pointer; font-weight: 600; }
.btn-primary { background: #2563eb; color: white; }
.btn-success { background: #16a34a; color: white; }

.success-msg { color: #16a34a; margin-top: 10px; }
.error-msg { color: #dc2626; margin-top: 10px; }
.empty-state { text-align: center; padding: 40px; color: #64748b; }
</style>
