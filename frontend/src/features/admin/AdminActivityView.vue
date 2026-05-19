<template>
  <div class="page">
    <main-layout>
      <template #title>Журнал активности</template>
      <template #content>
        <section class="admin-logs">
          <div class="table-card">
            <div class="card-header">
              <h2>Аудит действий</h2>
              <button @click="loadLogs" class="btn btn-secondary">Обновить</button>
            </div>

            <table class="table">
              <thead>
                <tr>
                  <th>Время</th>
                  <th>Пользователь</th>
                  <th>Действие</th>
                  <th>Подробности</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="log in logs" :key="log.id">
                  <td class="time-col">{{ formatDate(log.createdAt) }}</td>
                  <td class="user-col">👤 {{ log.username }}</td>
                  <td><span class="action-tag">{{ log.action }}</span></td>
                  <td class="details-col">{{ log.details || '—' }}</td>
                </tr>
              </tbody>
            </table>
            <div v-if="logs.length === 0" class="empty-state">Записей нет</div>
          </div>
        </section>
      </template>
    </main-layout>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import MainLayout from '../../layouts/MainLayout.vue';
import { fetchLogs } from '../../api/logs';
import type { ActivityLog } from '../../types';

const logs = ref<ActivityLog[]>([]);

async function loadLogs() {
  try {
    logs.value = await fetchLogs();
  } catch {
    console.error('Failed to load logs');
  }
}

function formatDate(value: string) {
  return new Date(value).toLocaleString('ru-RU', {
    day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit'
  });
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
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.card-header h2 { margin: 0; }

.table { width: 100%; border-collapse: collapse; }
th { text-align: left; padding: 14px; color: #64748b; border-bottom: 1px solid #e2e8f0; }
td { padding: 14px; border-bottom: 1px solid #f1f5f9; font-size: 0.95rem; }

.time-col { color: #94a3b8; white-space: nowrap; }
.user-col { font-weight: 600; color: #1e293b; }
.action-tag { background: #f1f5f9; padding: 4px 8px; border-radius: 6px; font-family: monospace; font-size: 0.85rem; }
.details-col { color: #475569; }

.btn { padding: 8px 16px; border-radius: 10px; border: none; cursor: pointer; font-weight: 600; }
.btn-secondary { background: #f1f5f9; color: #475569; }

.empty-state { text-align: center; padding: 40px; color: #64748b; }
</style>
