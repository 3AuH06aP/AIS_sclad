<template>
  <div class="page">
    <main-layout>
      <template #title>Текущие остатки</template>
      <template #content>
        <div class="reports-view">
          <div class="toolbar">
            <button @click="loadReport" class="btn btn-primary">Обновить данные</button>
            <button @click="exportToExcel" class="btn btn-success">Экспорт в Excel</button>
          </div>

          <div class="table-card">
            <table class="table">
              <thead>
                <tr>
                  <th>Артикул</th>
                  <th>Товар</th>
                  <th>Категория</th>
                  <th>Количество</th>
                  <th>Ед. изм.</th>
                  <th>Мин. остаток</th>
                  <th>Статус</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in stockData" :key="item.id">
                  <td>{{ item.sku }}</td>
                  <td>{{ item.name }}</td>
                  <td>{{ item.category || '—' }}</td>
                  <td :class="{ 'low-stock-text': isLowStock(item) }">
                    {{ item.quantity ?? 0 }}
                  </td>
                  <td>{{ item.unit || '—' }}</td>
                  <td>{{ item.minQuantity ?? 0 }}</td>
                  <td>
                    <span v-if="isLowStock(item)" class="badge badge-warn">Ниже минимума</span>
                    <span v-else class="badge badge-ok">В норме</span>
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-if="loading" class="empty-state">Загрузка данных...</div>
            <div v-if="!loading && stockData.length === 0" class="empty-state">
              Нет данных по остаткам
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

interface StockBalanceRow {
  id: number;
  sku: string;
  name: string;
  category?: string;
  unit?: string;
  quantity: number;
  minQuantity: number;
}

const stockData = ref<StockBalanceRow[]>([]);
const loading = ref(false);

async function loadReport() {
  loading.value = true;
  try {
    const response = await api.get('/reports/stock');
    stockData.value = response.data;
  } catch (error) {
    notify.error('Не удалось загрузить остатки', getApiErrorMessage(error));
  } finally {
    loading.value = false;
  }
}

function isLowStock(item: StockBalanceRow) {
  const min = item.minQuantity ?? 0;
  return (item.quantity ?? 0) <= min;
}

async function exportToExcel() {
  try {
    const response = await api.get('/reports/stock/export', { responseType: 'blob' });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', 'stock_report.xlsx');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  } catch (error) {
    notify.error('Не удалось экспортировать отчёт', getApiErrorMessage(error));
  }
}

onMounted(loadReport);
</script>

<style scoped>
.reports-view {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.toolbar {
  display: flex;
  gap: 12px;
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
.low-stock-text {
  color: #ef4444;
  font-weight: 700;
}
.badge {
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 600;
}
.badge-ok { background: #dcfce7; color: #166534; }
.badge-warn { background: #fee2e2; color: #991b1b; }
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
