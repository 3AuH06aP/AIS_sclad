<template>
  <div class="page">
    <main-layout>
      <template #title>Журнал действий администратора</template>
      <template #content>
        <section class="admin-logs">
          <div class="table-card">
            <div class="card-header">
              <h2>Аудит администрирования</h2>
              <button type="button" class="btn btn-secondary" @click="loadLogs">Обновить</button>
            </div>

            <div class="filters">
              <label class="filter-label">
                Администратор
                <input v-model="filters.admin" type="text" placeholder="Логин" class="input" />
              </label>
              <label class="filter-label">
                С
                <input v-model="filters.from" type="date" class="input" />
              </label>
              <label class="filter-label">
                По
                <input v-model="filters.to" type="date" class="input" />
              </label>
              <button type="button" class="btn btn-primary" @click="loadLogs">Применить</button>
              <button type="button" class="btn btn-ghost" @click="clearFilters">Сбросить</button>
            </div>

            <div v-if="loading" class="empty-state">Загрузка...</div>
            <table v-else class="table">
              <thead>
                <tr>
                  <th>Дата</th>
                  <th>Администратор</th>
                  <th>Действие</th>
                  <th>Целевой пользователь</th>
                  <th>Детали</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="log in logs" :key="log.id">
                  <td class="time-col">{{ formatDate(log.createdAt) }}</td>
                  <td class="user-col">{{ log.username }}</td>
                  <td><span class="action-tag">{{ actionLabel(log.action) }}</span></td>
                  <td>{{ log.targetUsername || '—' }}</td>
                  <td class="details-col">{{ log.details || '—' }}</td>
                </tr>
              </tbody>
            </table>
            <div v-if="!loading && logs.length === 0" class="empty-state">Записей нет</div>
          </div>
        </section>
      </template>
    </main-layout>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import MainLayout from '../../layouts/MainLayout.vue';
import { fetchAdminLogs, ADMIN_ACTION_LABELS } from '../../api/logs';
import type { ActivityLog } from '../../types';

const logs = ref<ActivityLog[]>([]);
const loading = ref(false);
const filters = ref({
  admin: '',
  from: '',
  to: ''
});

function actionLabel(action: string) {
  return ADMIN_ACTION_LABELS[action] || action;
}

function formatDate(value: string) {
  if (!value) return '—';
  const normalized = value.includes('T') ? value : value.replace(' ', 'T');
  return new Date(normalized).toLocaleString('ru-RU', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

function clearFilters() {
  filters.value = { admin: '', from: '', to: '' };
  loadLogs();
}

async function loadLogs() {
  loading.value = true;
  try {
    logs.value = await fetchAdminLogs({
      admin: filters.value.admin || undefined,
      from: filters.value.from || undefined,
      to: filters.value.to || undefined
    });
  } catch {
    logs.value = [];
  } finally {
    loading.value = false;
  }
}

onMounted(loadLogs);
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
  margin-bottom: 20px;
}
.card-header h2 {
  margin: 0;
}
.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: flex-end;
  margin-bottom: 20px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
}
.filter-label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 0.85rem;
  font-weight: 600;
  color: #64748b;
}
.input {
  padding: 10px 12px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  min-width: 160px;
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
  font-size: 0.95rem;
}
.time-col {
  color: #64748b;
  white-space: nowrap;
}
.user-col {
  font-weight: 600;
  color: #1e293b;
}
.action-tag {
  background: #f1f5f9;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 0.85rem;
  white-space: nowrap;
}
.details-col {
  color: #475569;
  max-width: 280px;
}
.btn {
  padding: 10px 18px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
  font-weight: 600;
}
.btn-primary {
  background: #2563eb;
  color: white;
}
.btn-secondary {
  background: #f1f5f9;
  color: #475569;
}
.btn-ghost {
  background: transparent;
  border: 1px solid #cbd5e1;
  color: #475569;
}
.empty-state {
  text-align: center;
  padding: 40px;
  color: #64748b;
}
</style>
