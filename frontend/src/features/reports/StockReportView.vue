<template>
  <div class="page">
    <main-layout>
      <template #title>Текущие остатки</template>
      <template #content>
        <div class="reports-view">
          <div class="toolbar">
            <input v-model="searchQuery" placeholder="Поиск по названию, артикулу или категории..." class="search-input" />
            <div class="actions">
              <button @click="loadReport" class="btn btn-primary">Обновить данные</button>
              <button @click="exportToExcel" class="btn btn-success">Экспорт в Excel</button>
            </div>
          </div>

          <div class="table-card">
            <table class="table">
              <thead>
                <tr>
                  <th @click="toggleSort('sku')" class="sortable">
                    Артикул <span class="sort-icon">{{ getSortIcon('sku') }}</span>
                  </th>
                  <th @click="toggleSort('name')" class="sortable">
                    Товар <span class="sort-icon">{{ getSortIcon('name') }}</span>
                  </th>
                  <th @click="toggleSort('category')" class="sortable">
                    Категория <span class="sort-icon">{{ getSortIcon('category') }}</span>
                  </th>
                  <th @click="toggleSort('quantity')" class="sortable">
                    Количество <span class="sort-icon">{{ getSortIcon('quantity') }}</span>
                  </th>
                  <th>Ед. изм.</th>
                  <th @click="toggleSort('minQuantity')" class="sortable">
                    Мин. остаток <span class="sort-icon">{{ getSortIcon('minQuantity') }}</span>
                  </th>
                  <th>Статус</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in sortedStockData" :key="item.id">
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
            <div v-if="!loading && sortedStockData.length === 0" class="empty-state">
              Записей не найдено
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
import { ref, computed, onMounted } from 'vue';
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
const searchQuery = ref('');

// Sorting state
const sortKey = ref('name');
const sortOrder = ref(1); // ASC by default

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

const filteredStockData = computed(() => {
  const query = searchQuery.value.toLowerCase().trim();
  if (!query) return stockData.value;

  return stockData.value.filter(item => {
    return item.name.toLowerCase().includes(query) ||
           item.sku.toLowerCase().includes(query) ||
           (item.category && item.category.toLowerCase().includes(query));
  });
});

const sortedStockData = computed(() => {
  const list = [...filteredStockData.value];
  if (sortOrder.value === 0 || !sortKey.value) return list;

  return list.sort((a, b) => {
    const valA = (a as any)[sortKey.value];
    const valB = (b as any)[sortKey.value];

    if (valA === valB) return 0;

    if (typeof valA === 'number' && typeof valB === 'number') {
      return sortOrder.value === 1 ? valA - valB : valB - valA;
    }

    const strA = String(valA || '').toLowerCase();
    const strB = String(valB || '').toLowerCase();

    return sortOrder.value === 1
      ? strA.localeCompare(strB)
      : strB.localeCompare(strA);
  });
});

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
.reports-view { display: flex; flex-direction: column; gap: 20px; }
.toolbar { display: flex; gap: 16px; align-items: center; justify-content: space-between; flex-wrap: wrap; }
.search-input { flex: 1; min-width: 300px; padding: 12px 16px; border-radius: 12px; border: 1px solid var(--border-color); background: var(--bg-card); color: var(--text-main); }
.actions { display: flex; gap: 12px; }
.table-card { background: var(--bg-card); border-radius: 18px; padding: 20px; box-shadow: var(--shadow); }
.table { width: 100%; border-collapse: collapse; }
th { text-align: left; padding: 14px; color: var(--text-muted); border-bottom: 1px solid var(--border-color); }
th.sortable { cursor: pointer; user-select: none; }
th.sortable:hover { background: #f8fafc; }
[data-theme='light'] th.sortable:hover { background: #f1f5f9; }
.sort-icon { display: inline-block; margin-left: 4px; width: 12px; color: var(--text-muted); }
td { padding: 14px; border-bottom: 1px solid var(--border-color); color: var(--text-main); }
.low-stock-text { color: #ef4444; font-weight: 700; }
.badge { padding: 4px 8px; border-radius: 6px; font-size: 0.85rem; font-weight: 600; }
.badge-ok { background: #dcfce7; color: #166534; }
.badge-warn { background: #fee2e2; color: #991b1b; }
.btn { padding: 10px 16px; border-radius: 10px; border: none; cursor: pointer; font-weight: 600; }
.btn-primary { background: var(--accent-primary); color: white; }
.btn-success { background: #10b981; color: white; }
.empty-state { text-align: center; padding: 40px; color: var(--text-muted); }
</style>
