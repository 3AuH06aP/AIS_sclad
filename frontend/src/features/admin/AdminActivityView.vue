<template>
  <div class="page">
    <main-layout>
      <template #title>Журнал действий системы</template>
      <template #content>
        <section class="admin-logs">
          <div class="table-card">
            <div class="card-header">
              <h2>Аудит и мониторинг</h2>
              <button type="button" class="btn btn-secondary" @click="loadLogs">Обновить</button>
            </div>

            <!-- Улучшенные фильтры -->
            <div class="filters-panel">
              <div class="filters-grid">
                <label class="filter-label">
                  Пользователь
                  <input v-model="filters.user" type="text" placeholder="Логин" class="input" />
                </label>
                <label class="filter-label">
                  Действие
                  <input v-model="filters.action" type="text" placeholder="Поиск по действию" class="input" />
                </label>
                <label class="filter-label">
                  С
                  <input v-model="filters.from" type="date" class="input" />
                </label>
                <label class="filter-label">
                  По
                  <input v-model="filters.to" type="date" class="input" />
                </label>
                <div class="filter-actions">
                  <button type="button" class="btn btn-primary" @click="loadLogs">Применить</button>
                  <button type="button" class="btn btn-ghost" @click="clearFilters">Сбросить</button>
                </div>
              </div>
            </div>

            <div v-if="loading" class="empty-state">Загрузка данных...</div>
            <table v-else class="table">
              <thead>
                <tr>
                  <th @click="toggleSort('createdAt')" class="sortable">
                    Дата <span class="sort-icon">{{ getSortIcon('createdAt') }}</span>
                  </th>
                  <th @click="toggleSort('username')" class="sortable">
                    Пользователь <span class="sort-icon">{{ getSortIcon('username') }}</span>
                  </th>
                  <th @click="toggleSort('action')" class="sortable">
                    Действие <span class="sort-icon">{{ getSortIcon('action') }}</span>
                  </th>
                  <th>Целевой объект</th>
                  <th>Детали</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="log in sortedLogs" :key="log.id">
                  <td class="time-col">{{ formatDate(log.createdAt) }}</td>
                  <td class="user-col">👤 {{ log.username }}</td>
                  <td><span class="action-tag">{{ actionLabel(log.action) }}</span></td>
                  <td>{{ log.targetUsername || '—' }}</td>
                  <td class="details-col">{{ log.details || '—' }}</td>
                </tr>
              </tbody>
            </table>
            <div v-if="!loading && sortedLogs.length === 0" class="empty-state">Записей не найдено</div>
          </div>
        </section>
      </template>
    </main-layout>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import MainLayout from '../../layouts/MainLayout.vue';
import api from '../../api/http';
import type { ActivityLog } from '../../types';

// Маппинг действий для красивого отображения (опционально)
const ACTION_LABELS: Record<string, string> = {
  'login_success': 'Вход в систему',
  'SAVE_DOCUMENT': 'Сохранение документа',
  'CONFIRM_DOCUMENT': 'Проведение документа',
  'DELETE_DOCUMENT': 'Удаление документа',
  'create_stock_item': 'Создание остатка',
  'adjust_stock': 'Корректировка склада',
  'admin_user_create': 'Создание пользователя',
  'admin_user_block': 'Блокировка',
  'admin_user_unblock': 'Разблокировка'
};

const logs = ref<ActivityLog[]>([]);
const loading = ref(false);
const filters = ref({
  user: '',
  action: '',
  from: '',
  to: ''
});

// Сортировка (3 состояния)
const sortKey = ref('createdAt');
const sortOrder = ref(-1); // По умолчанию новые сверху

function toggleSort(key: string) {
  if (sortKey.value === key) {
    if (sortOrder.value === 0) sortOrder.value = 1;
    else if (sortOrder.value === 1) sortOrder.value = -1;
    else sortOrder.value = 0;
  } else {
    sortKey.value = key;
    sortOrder.value = 1;
  }
}

function getSortIcon(key: string) {
  if (sortKey.value !== key || sortOrder.value === 0) return '↕';
  return sortOrder.value === 1 ? '↑' : '↓';
}

const sortedLogs = computed(() => {
  if (sortOrder.value === 0 || !sortKey.value) {
    return logs.value;
  }

  return [...logs.value].sort((a, b) => {
    const valA = (a as any)[sortKey.value];
    const valB = (b as any)[sortKey.value];

    if (valA === valB) return 0;

    const strA = String(valA || '').toLowerCase();
    const strB = String(valB || '').toLowerCase();

    return sortOrder.value === 1
      ? strA.localeCompare(strB)
      : strB.localeCompare(strA);
  });
});

function actionLabel(action: string) {
  return ACTION_LABELS[action] || action;
}

function formatDate(value: string) {
  if (!value) return '—';
  return new Date(value).toLocaleString('ru-RU');
}

function clearFilters() {
  filters.value = { user: '', action: '', from: '', to: '' };
  loadLogs();
}

async function loadLogs() {
  loading.value = true;
  try {
    const params: any = {};
    if (filters.value.user) params.user = filters.value.user;
    if (filters.value.action) params.action = filters.value.action;
    if (filters.value.from) params.from = filters.value.from;
    if (filters.value.to) params.to = filters.value.to;

    const response = await api.get('/logs', { params });
    logs.value = response.data;
  } catch (error) {
    console.error('Failed to load logs:', error);
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
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.card-header h2 { margin: 0; }

.filters-panel {
  background: #f8fafc;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 24px;
  border: 1px solid #e2e8f0;
}
.filters-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: flex-end;
}
.filter-label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 0.85rem;
  font-weight: 600;
  color: #64748b;
  flex: 1;
  min-width: 150px;
}
.input {
  padding: 10px 12px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 0.9rem;
}
.filter-actions {
  display: flex;
  gap: 8px;
}

.table { width: 100%; border-collapse: collapse; }
th { text-align: left; padding: 14px; color: #64748b; border-bottom: 1px solid #e2e8f0; font-size: 0.85rem; }
th.sortable { cursor: pointer; user-select: none; }
th.sortable:hover { background: #f1f5f9; }
.sort-icon { display: inline-block; margin-left: 4px; width: 12px; color: #94a3b8; }
td { padding: 14px; border-bottom: 1px solid #f1f5f9; font-size: 0.95rem; }

.time-col { color: #64748b; white-space: nowrap; }
.user-col { font-weight: 600; color: #1e293b; }
.action-tag { background: #f1f5f9; padding: 4px 10px; border-radius: 6px; font-size: 0.85rem; color: #2563eb; }
.details-col { color: #475569; }

.btn { padding: 10px 18px; border-radius: 10px; border: none; cursor: pointer; font-weight: 600; }
.btn-primary { background: #2563eb; color: white; }
.btn-secondary { background: #f1f5f9; color: #475569; }
.btn-ghost { background: transparent; border: 1px solid #cbd5e1; color: #475569; }

.empty-state { text-align: center; padding: 40px; color: #64748b; }
</style>
