<template>
  <div class="page">
    <main-layout>
      <template #title>Журнал активности</template>
      <template #content>
        <section class="activity-panel">
          <div class="panel-card">
            <h2>Аудит действий</h2>
            <table>
              <thead>
                <tr>
                  <th>Пользователь</th>
                  <th>Действие</th>
                  <th>Детали</th>
                  <th>Время</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="log in logs" :key="log.id">
                  <td>{{ log.username }}</td>
                  <td>{{ log.action }}</td>
                  <td>{{ log.details || '—' }}</td>
                  <td>{{ formatDate(log.createdAt) }}</td>
                </tr>
              </tbody>
            </table>
            <div v-if="logs.length === 0" class="empty-state">Записи журнала отсутствуют.</div>
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
const error = ref('');

async function loadLogs() {
  error.value = '';
  try {
    logs.value = await fetchLogs();
  } catch {
    error.value = 'Не удалось загрузить журнал активности.';
  }
}

function formatDate(value: string) {
  return new Date(value).toLocaleString('ru-RU', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
}

onMounted(loadLogs);
</script>

<style scoped>
.activity-panel {
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
}
th,
td {
  padding: 14px 12px;
  border-bottom: 1px solid #e2e8f0;
  text-align: left;
}
.empty-state {
  margin-top: 16px;
  color: #475569;
}
</style>
