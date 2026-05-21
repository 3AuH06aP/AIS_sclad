<template>
  <div class="page">
    <main-layout>
      <template #title>Управление пользователями</template>
      <template #content>
        <section class="admin-users">
          <div class="table-card">
            <div class="card-header">
              <h2>Пользователи системы</h2>
              <button type="button" class="btn btn-primary" @click="showCreate = !showCreate">
                {{ showCreate ? 'Скрыть форму' : '+ Добавить пользователя' }}
              </button>
            </div>

            <div v-if="showCreate" class="create-form panel-card">
              <h3>Новый кладовщик (STOREKEEPER)</h3>
              <p class="hint">Роль при создании фиксирована: кладовщик (в таблице отображается как USER).</p>
              <form class="form-grid" @submit.prevent="submit">
                <div class="form-group">
                  <label>Логин</label>
                  <input v-model="form.username" placeholder="Логин" required autocomplete="off" />
                </div>
                <div class="form-group">
                  <label>Пароль</label>
                  <div class="password-row">
                    <input v-model="form.password" type="text" placeholder="Минимум 4 символа" required />
                    <button type="button" class="btn btn-secondary" @click="generatePassword">Сгенерировать</button>
                  </div>
                </div>
                <div class="form-group">
                  <label>Подтверждение пароля</label>
                  <input v-model="form.passwordConfirm" type="text" placeholder="Повторите пароль" required />
                </div>
                <div class="form-actions">
                  <button type="submit" class="btn btn-success" :disabled="submitting">Создать</button>
                </div>
              </form>
              <p v-if="message" class="success-msg">{{ message }}</p>
              <p v-if="error" class="error-msg">{{ error }}</p>
            </div>

            <div v-if="loading" class="empty-state">Загрузка...</div>
            <table v-else class="table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Логин</th>
                  <th>Роль</th>
                  <th>Дата регистрации</th>
                  <th>Активен</th>
                  <th>Действия</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="user in users" :key="user.id">
                  <td>#{{ user.id }}</td>
                  <td class="user-name">{{ user.username }}</td>
                  <td>
                    <span :class="['role-tag', user.role]">{{ roleLabel(user.role) }}</span>
                  </td>
                  <td class="date-col">{{ formatDate(user.createdAt) }}</td>
                  <td>
                    <span :class="user.enabled ? 'status-on' : 'status-off'">
                      {{ user.enabled ? 'Да' : 'Нет' }}
                    </span>
                  </td>
                  <td class="actions-col">
                    <button
                      type="button"
                      class="btn btn-sm btn-secondary"
                      :disabled="actionUserId === user.id"
                      @click="openResetModal(user)"
                    >
                      Сбросить пароль
                    </button>
                    <button
                      type="button"
                      class="btn btn-sm btn-ghost"
                      :disabled="actionUserId === user.id"
                      @click="toggleBlock(user)"
                    >
                      {{ user.enabled ? 'Заблокировать' : 'Разблокировать' }}
                    </button>
                    <button
                      type="button"
                      class="btn btn-sm btn-danger"
                      :disabled="actionUserId === user.id || isSelf(user)"
                      :title="isSelf(user) ? 'Нельзя удалить свою учётную запись' : ''"
                      @click="confirmDelete(user)"
                    >
                      Удалить
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-if="!loading && users.length === 0" class="empty-state">Нет пользователей</div>
          </div>
        </section>

        <div v-if="resetModal.open" class="modal-overlay" @click.self="closeResetModal">
          <div class="modal-card">
            <h3>Новый пароль</h3>
            <p>
              Пользователь <strong>{{ resetModal.username }}</strong
              >. Сохраните пароль — повторно он не будет показан.
            </p>
            <div class="password-display">{{ resetModal.password }}</div>
            <div class="modal-actions">
              <button type="button" class="btn btn-primary" @click="copyPassword">Копировать</button>
              <button type="button" class="btn btn-ghost" @click="closeResetModal">Закрыть</button>
            </div>
          </div>
        </div>
      </template>
    </main-layout>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import MainLayout from '../../layouts/MainLayout.vue';
import {
  fetchUsers,
  createUser,
  resetUserPassword,
  setUserEnabled,
  deleteUser,
  generatePassword as genPassword
} from '../../api/users';
import { useAuthStore } from '../../stores/auth';
import type { User, ApiUserRole } from '../../types';
import { getApiErrorMessage } from '../../utils/apiError';

const auth = useAuthStore();

const users = ref<User[]>([]);
const loading = ref(false);
const submitting = ref(false);
const actionUserId = ref<number | null>(null);
const showCreate = ref(false);
const message = ref('');
const error = ref('');

const form = ref({
  username: '',
  password: '',
  passwordConfirm: ''
});

const resetModal = ref({
  open: false,
  username: '',
  password: ''
});

function roleLabel(role: ApiUserRole) {
  return role === 'admin' ? 'ADMIN' : 'USER';
}

function formatDate(value?: string | null) {
  if (!value) return '—';
  try {
    return new Intl.DateTimeFormat('ru-RU', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value));
  } catch {
    return value;
  }
}

function isSelf(user: User) {
  return user.username === auth.user;
}

function generatePassword() {
  const pwd = genPassword(10);
  form.value.password = pwd;
  form.value.passwordConfirm = pwd;
}

async function loadUsers() {
  loading.value = true;
  error.value = '';
  try {
    users.value = await fetchUsers();
  } catch (e) {
    error.value = getApiErrorMessage(e) || 'Ошибка загрузки';
  } finally {
    loading.value = false;
  }
}

async function submit() {
  error.value = '';
  message.value = '';
  if (form.value.password !== form.value.passwordConfirm) {
    error.value = 'Пароли не совпадают';
    return;
  }
  submitting.value = true;
  try {
    await createUser({
      username: form.value.username.trim(),
      password: form.value.password,
      passwordConfirm: form.value.passwordConfirm
    });
    message.value = 'Пользователь создан';
    form.value = { username: '', password: '', passwordConfirm: '' };
    showCreate.value = false;
    await loadUsers();
  } catch (e) {
    error.value = getApiErrorMessage(e) || 'Ошибка создания';
  } finally {
    submitting.value = false;
  }
}

async function openResetModal(user: User) {
  if (!confirm(`Сбросить пароль для «${user.username}»?`)) return;
  actionUserId.value = user.id;
  try {
    const result = await resetUserPassword(user.id);
    resetModal.value = {
      open: true,
      username: result.username,
      password: result.newPassword
    };
  } catch (e) {
    alert(getApiErrorMessage(e) || 'Не удалось сбросить пароль');
  } finally {
    actionUserId.value = null;
  }
}

function closeResetModal() {
  resetModal.value = { open: false, username: '', password: '' };
}

async function copyPassword() {
  try {
    await navigator.clipboard.writeText(resetModal.value.password);
    message.value = 'Пароль скопирован в буфер обмена';
  } catch {
    message.value = 'Скопируйте пароль вручную';
  }
}

async function toggleBlock(user: User) {
  const next = !user.enabled;
  const verb = next ? 'разблокировать' : 'заблокировать';
  if (!confirm(`${verb.charAt(0).toUpperCase() + verb.slice(1)} пользователя «${user.username}»?`)) return;
  actionUserId.value = user.id;
  try {
    await setUserEnabled(user.id, next);
    await loadUsers();
  } catch (e) {
    alert(getApiErrorMessage(e) || 'Ошибка изменения статуса');
  } finally {
    actionUserId.value = null;
  }
}

async function confirmDelete(user: User) {
  if (!confirm(`Удалить пользователя «${user.username}»? Это действие необратимо.`)) return;
  actionUserId.value = user.id;
  try {
    await deleteUser(user.id);
    message.value = `Пользователь ${user.username} удалён`;
    await loadUsers();
  } catch (e) {
    alert(getApiErrorMessage(e) || 'Не удалось удалить пользователя');
  } finally {
    actionUserId.value = null;
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
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 12px;
}
.card-header h2 {
  margin: 0;
}
.hint {
  margin: 0 0 12px 0;
  color: #64748b;
  font-size: 0.9rem;
}
.create-form {
  background: #f8fafc;
  margin-bottom: 24px;
  border: 1px solid #e2e8f0;
}
.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
  align-items: flex-end;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.password-row {
  display: flex;
  gap: 8px;
}
.password-row input {
  flex: 1;
}
label {
  font-size: 0.9rem;
  font-weight: 600;
  color: #64748b;
}
input {
  padding: 10px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
}
.table {
  width: 100%;
  border-collapse: collapse;
}
th {
  text-align: left;
  padding: 14px;
  color: #64748b;
  border-bottom: 1px solid #e2e8f0;
  font-size: 0.85rem;
}
td {
  padding: 14px;
  border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
}
.user-name {
  font-weight: 600;
  color: #0f172a;
}
.date-col {
  white-space: nowrap;
  color: #64748b;
  font-size: 0.9rem;
}
.actions-col {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.role-tag {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.02em;
}
.role-tag.admin {
  background: #fee2e2;
  color: #b91c1c;
}
.role-tag.user {
  background: #e0f2fe;
  color: #0369a1;
}
.status-on {
  color: #16a34a;
  font-weight: 600;
}
.status-off {
  color: #dc2626;
  font-weight: 600;
}
.btn {
  padding: 10px 16px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.9rem;
}
.btn-sm {
  padding: 6px 12px;
  font-size: 0.8rem;
}
.btn-primary {
  background: #2563eb;
  color: white;
}
.btn-secondary {
  background: #f1f5f9;
  color: #334155;
  border: 1px solid #e2e8f0;
}
.btn-success {
  background: #16a34a;
  color: white;
}
.btn-ghost {
  background: transparent;
  border: 1px solid #cbd5e1;
  color: #475569;
}
.btn-danger {
  background: #fee2e2;
  color: #b91c1c;
  border: 1px solid #fecaca;
}
.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.success-msg {
  color: #16a34a;
  margin-top: 10px;
}
.error-msg {
  color: #dc2626;
  margin-top: 10px;
}
.empty-state {
  text-align: center;
  padding: 40px;
  color: #64748b;
}
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: 24px;
}
.modal-card {
  background: white;
  border-radius: 16px;
  padding: 28px;
  max-width: 420px;
  width: 100%;
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.15);
}
.modal-card h3 {
  margin: 0 0 12px 0;
}
.password-display {
  font-family: ui-monospace, monospace;
  font-size: 1.25rem;
  font-weight: 700;
  padding: 16px;
  background: #f8fafc;
  border-radius: 10px;
  text-align: center;
  letter-spacing: 0.05em;
  margin: 16px 0;
}
.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}
</style>
