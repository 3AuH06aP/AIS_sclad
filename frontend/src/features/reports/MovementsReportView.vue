<template>
  <div class="page">
    <main-layout>
      <template #title>Движения за период</template>
      <template #content>
        <div class="reports-view">
          <div class="toolbar">
            <input v-model="searchQuery" placeholder="Поиск по товару, складу или автору..." class="search-input" />
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
            <div class="actions">
              <button @click="loadReport" class="btn btn-primary">Показать</button>
              <button @click="exportToExcel" class="btn btn-success">Экспорт в Excel</button>
            </div>
          </div>

          <div class="table-card">
            <table class="table">
              <thead>
                <tr>
                  <th @click="toggleSort('transactionDate')" class="sortable">
                    Дата <span class="sort-icon">{{ getSortIcon('transactionDate') }}</span>
                  </th>
                  <th @click="toggleSort('type')" class="sortable">
                    Тип <span class="sort-icon">{{ getSortIcon('type') }}</span>
                  </th>
                  <th @click="toggleSort('productName')" class="sortable">
                    Товар <span class="sort-icon">{{ getSortIcon('productName') }}</span>
                  </th>
                  <th @click="toggleSort('warehouseName')" class="sortable">
                    Склад <span class="sort-icon">{{ getSortIcon('warehouseName') }}</span>
                  </th>
                  <th @click="toggleSort('quantity')" class="sortable">
                    Кол-во <span class="sort-icon">{{ getSortIcon('quantity') }}</span>
                  </th>
                  <th @click="toggleSort('createdBy')" class="sortable">
                    Автор <span class="sort-icon">{{ getSortIcon('createdBy') }}</span>
                  </th>
                  <th>Комментарий</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="tx in sortedMovements" :key="tx.id">
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
            <div v-if="!loading && sortedMovements.length === 0" class="empty-state">
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
const searchQuery = ref('');
const filter = ref({
  from: '',
  to: ''
});

// Sorting state
const sortKey = ref('transactionDate');
const sortOrder = ref(-1); // Newest first

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

const filteredMovements = computed(() => {
  const query = searchQuery.value.toLowerCase().trim();
  if (!query) return movements.value;

  return movements.value.filter(tx => {
    return tx.productName.toLowerCase().includes(query) ||
           tx.productSku.toLowerCase().includes(query) ||
           tx.warehouseName.toLowerCase().includes(query) ||
           (tx.createdBy && tx.createdBy.toLowerCase().includes(query));
  });
});

const sortedMovements = computed(() => {
  const list = [...filteredMovements.value];
  if (sortOrder.value === 0 || !sortKey.value) return list;

  return list.sort((a, b) => {
    const valA = (a as any)[sortKey.value];
    const valB = (b as any)[sortKey.value];

    if (valA === valB) return 0;

    if (typeof valA === 'number' && typeof valB === 'number') {
      return sortOrder.value === 1 ? valA - valB : valB - valA;
    }

    return sortOrder.value === 1
      ? String(valA || '').localeCompare(String(valB || ''))
      : String(valB || '').localeCompare(String(valA || ''));
  });
});

function defaultDateRange() {
  const today = new Date();
  const start = new Date(today.getFullYear(), today.getMonth(), 1);
  filter.value.to = today.toISOString().slice(0, 10);
  filter.value.from = start.toISOString().slice(0, 10);
}

function buildParams() {
  const params: Record<string, string> = {};
  if (filter.value.from) params.from = filter.value.from;
  if (filter.value.to) params.to = filter.value.to;
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
.reports-view { display: flex; flex-direction: column; gap: 20px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; gap: 16px; flex-wrap: wrap; }
.search-input { flex: 1; min-width: 250px; padding: 12px 16px; border-radius: 12px; border: 1px solid var(--border-color); background: var(--bg-card); color: var(--text-main); }
.filters { display: flex; gap: 12px; align-items: flex-end; }
.filter-label { display: flex; flex-direction: column; gap: 4px; font-size: 0.85rem; color: var(--text-muted); }
.input { padding: 8px 12px; border-radius: 8px; border: 1px solid var(--border-color); background: var(--bg-card); color: var(--text-main); }
.actions { display: flex; gap: 12px; }
.table-card { background: var(--bg-card); border-radius: 18px; padding: 20px; box-shadow: var(--shadow); }
.table { width: 100%; border-collapse: collapse; }
th { text-align: left; padding: 14px; color: var(--text-muted); border-bottom: 1px solid var(--border-color); }
th.sortable { cursor: pointer; user-select: none; }
th.sortable:hover { background: #f8fafc; }
[data-theme='light'] th.sortable:hover { background: #f1f5f9; }
.sort-icon { display: inline-block; margin-left: 4px; width: 12px; color: var(--text-muted); }
td { padding: 14px; border-bottom: 1px solid var(--border-color); color: var(--text-main); }
.badge { padding: 4px 8px; border-radius: 6px; font-size: 0.85rem; font-weight: 600; }
.badge-in { background: #dcfce7; color: #166534; }
.badge-out { background: #fee2e2; color: #991b1b; }
.btn { padding: 10px 16px; border-radius: 10px; border: none; cursor: pointer; font-weight: 600; }
.btn-primary { background: var(--accent-primary); color: white; }
.btn-success { background: #10b981; color: white; }
.empty-state { text-align: center; padding: 40px; color: var(--text-muted); }
</style>
