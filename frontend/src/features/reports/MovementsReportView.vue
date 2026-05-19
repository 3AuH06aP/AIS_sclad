<template>
  <div class="page">
    <main-layout>
      <template #title>Движения за период</template>
      <template #content>
        <div class="reports-view">
          <div class="toolbar">
            <div class="filters">
              <label class="filter-label">
                С
                <input type="date" v-model="filter.from" class="input">
              </label>
              <label class="filter-label">
                По
                <input type="date" v-model="filter.to" class="input">
              </label>
            </div>
            <button @click="loadReport" class="btn btn-primary">Показать</button>
            <button @click="exportToExcel" class="btn btn-success">Экспорт в Excel</button>
          </div>

          <div class="table-card">
            <table class="table">
              <thead>
                <tr>
                  <th>Дата</th>
                  <th>Тип</th>
                  <th>Товар</th>
                  <th>Склад</th>
                  <th>Кол-во</th>
                  <th>Автор</th>
                  <th>Комментарий</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="tx in movements" :key="tx.id">
                  <td>{{ formatDate(tx.transactionDate) }}</td>
                  <td>
                    <span :class="['badge', tx.type === 'IN' ? 'badge-in' : 'badge-out']">
                      {{ tx.type === 'IN' ? 'Приход' : 'Расход' }}
                    </span>
                  </td>
                  <td>{{ tx.productName }} ({{ tx.productSku }})</td>
                  <td>{{ tx.warehouseName }}</td>
                  <td>{{ tx.quantity }}</td>
                  <td>{{ tx.createdBy || '—' }}</td>
                  <td>{{ tx.notes || tx.reference || '—' }}</td>
                </tr>
              </tbody>
            </table>
            <div v-if="loading" class="empty-state">Загрузка данных...</div>
            <div v-if="!loading && movements.length === 0" class="empty-state">
              Нет данных за выбранный период
            </div>
          </div>
        </div>
      </template>
    </main-layout>
  </div>
</template>

<script setup lang="ts">
import MainLayout from '../../layouts/MainLayout.vue';
import api from '../../api/http';
import { ref, onMounted } from 'vue';
import { useNotifyStore } from '../../stores/notify';
import { getApiErrorMessage } from '../../utils/apiError';

const notify = useNotifyStore();

interface MovementRow {
  id: number;
  transactionDate: string;
  type: 'IN' | 'OUT';
  productSku: string;
  productName: string;
  warehouseName: string;
  quantity: number;
  createdBy?: string;
  notes?: string;
  reference?: string;
}

const movements = ref<MovementRow[]>([]);
const loading = ref(false);
const filter = ref({
  from: '',
  to: ''
});

function defaultDateRange() {
  const today = new Date();
  const start = new Date(today.getFullYear(), today.getMonth(), 1);
  filter.value.to = today.toISOString().slice(0, 10);
  filter.value.from = start.toISOString().slice(0, 10);
}

function buildParams() {
  const params: Record<string, string> = {};
  if (filter.value.from) {
    params.from = filter.value.from;
  }
  if (filter.value.to) {
    params.to = filter.value.to;
  }
  return params;
}

async function loadReport() {
  loading.value = true;
  try {
    const response = await api.get('/reports/transactions', { params: buildParams() });
    movements.value = response.data;
  } catch (error) {
    movements.value = [];
    notify.error('Не удалось загрузить отчёт', getApiErrorMessage(error));
  } finally {
    loading.value = false;
  }
}

function formatDate(dateStr: string) {
  if (!dateStr) return '—';
  return new Date(dateStr).toLocaleString('ru-RU');
}

async function exportToExcel() {
  try {
    const response = await api.get('/reports/transactions/export', {
      params: buildParams(),
      responseType: 'blob'
    });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', 'movements_report.xlsx');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  } catch (error) {
    notify.error('Не удалось экспортировать отчёт', getApiErrorMessage(error));
  }
}

onMounted(() => {
  defaultDateRange();
  loadReport();
});
</script>

<style scoped>
.reports-view {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.filters {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}
.filter-label {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 0.85rem;
  color: #64748b;
}
.input {
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}
.table-card {
  background: white;
  border-radius: 18px;
  padding: 20px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.06);
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
}
td {
  padding: 14px;
  border-bottom: 1px solid #f1f5f9;
}
.badge {
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 600;
}
.badge-in { background: #dcfce7; color: #166534; }
.badge-out { background: #fee2e2; color: #991b1b; }
.btn {
  padding: 10px 16px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
  font-weight: 600;
}
.btn-primary { background: #2563eb; color: white; }
.btn-success { background: #10b981; color: white; }
.empty-state {
  text-align: center;
  padding: 40px;
  color: #64748b;
}
</style>
