<template>
  <div class="page">
    <main-layout>
      <template #title>Документы</template>
      <template #content>
        <div class="documents-view">
          <section class="toolbar">
            <input v-model="searchQuery" placeholder="Поиск по номеру, типу или статусу..." class="search-input" />
            <div class="actions">
              <button @click="create('receipt')" class="btn btn-primary">Приёмка</button>
              <button @click="create('shipment')" class="btn btn-secondary">Отгрузка</button>
              <button @click="create('transfer')" class="btn btn-info">Перемещение</button>
              <button @click="create('write-off')" class="btn btn-warning">Списание</button>
            </div>
          </section>

          <div class="table-card">
            <table class="table">
              <thead>
                <tr>
                  <th @click="toggleSort('documentNumber')" class="sortable">
                    Номер <span class="sort-icon">{{ getSortIcon('documentNumber') }}</span>
                  </th>
                  <th @click="toggleSort('documentType')" class="sortable">
                    Тип <span class="sort-icon">{{ getSortIcon('documentType') }}</span>
                  </th>
                  <th @click="toggleSort('status')" class="sortable">
                    Статус <span class="sort-icon">{{ getSortIcon('status') }}</span>
                  </th>
                  <th @click="toggleSort('createdAt')" class="sortable">
                    Создан <span class="sort-icon">{{ getSortIcon('createdAt') }}</span>
                  </th>
                  <th>Действия</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="doc in sortedDocuments" :key="doc.id">
                  <td>
                    <router-link :to="'/documents/' + doc.id" class="doc-link">
                      {{ doc.documentNumber || doc.id }}
                    </router-link>
                  </td>
                  <td>{{ getDocumentTypeLabel(doc.documentType) }}</td>
                  <td>
                    <span :class="['status-badge', doc.status.toLowerCase()]">
                      {{ getStatusLabel(doc.status) }}
                    </span>
                  </td>
                  <td>{{ formatDate(doc.createdAt) }}</td>
                  <td class="table-actions">
                    <button v-if="doc.status === 'DRAFT'" @click="confirmDocument(doc.id)" class="btn btn-sm btn-success">Подтвердить</button>
                    <router-link :to="'/documents/' + doc.id" class="btn btn-sm btn-secondary">Карточка</router-link>
                    <button @click="deleteDocument(doc.id)" class="btn btn-sm btn-danger">Удалить</button>
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-if="loading" class="empty-state">Загрузка...</div>
            <div v-if="!loading && sortedDocuments.length === 0" class="empty-state">Документы не найдены</div>
          </div>
        </div>
      </template>
    </main-layout>
  </div>
</template>

<script setup lang="ts">
import MainLayout from '../../layouts/MainLayout.vue';
import { fetchDocuments, confirmDocument as apiConfirmDocument, deleteDocument as apiDeleteDocument } from '../../api/documents';
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useNotifyStore } from '../../stores/notify';
import { getApiErrorMessage } from '../../utils/apiError';

const router = useRouter();
const notify = useNotifyStore();
const documents = ref([]);
const loading = ref(false);
const searchQuery = ref('');

// Sorting state
const sortKey = ref('createdAt');
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

const filteredDocuments = computed(() => {
  const query = searchQuery.value.toLowerCase().trim();
  if (!query) return documents.value;

  return documents.value.filter(doc => {
    const typeLabel = getDocumentTypeLabel(doc.documentType).toLowerCase();
    const statusLabel = getStatusLabel(doc.status).toLowerCase();
    const num = (doc.documentNumber || doc.id.toString()).toLowerCase();

    return num.includes(query) ||
           typeLabel.includes(query) ||
           statusLabel.includes(query);
  });
});

const sortedDocuments = computed(() => {
  const list = [...filteredDocuments.value];
  if (sortOrder.value === 0 || !sortKey.value) return list;

  return list.sort((a, b) => {
    const valA = (a as any)[sortKey.value];
    const valB = (b as any)[sortKey.value];

    if (valA === valB) return 0;

    if (sortOrder.value === 1) {
      return String(valA || '').localeCompare(String(valB || ''), undefined, { numeric: true });
    } else {
      return String(valB || '').localeCompare(String(valA || ''), undefined, { numeric: true });
    }
  });
});

async function loadDocuments() {
  loading.value = true;
  try {
    documents.value = await fetchDocuments();
  } catch (error) {
    notify.error('Не удалось загрузить документы', getApiErrorMessage(error));
  } finally {
    loading.value = false;
  }
}

function create(type: string) {
  router.push(`/documents/create/${type}`);
}

async function confirmDocument(id: number) {
  try {
    await apiConfirmDocument(id);
    notify.success('Документ проведён');
    await loadDocuments();
  } catch (error) {
    notify.error('Не удалось подтвердить документ', getApiErrorMessage(error));
  }
}

async function deleteDocument(id: number) {
  const confirmed = await notify.confirm(
    'Удалить документ?',
    'Черновик будет удалён без возможности восстановления.'
  );
  if (!confirmed) return;

  try {
    await apiDeleteDocument(id);
    notify.success('Документ удалён');
    await loadDocuments();
  } catch (error) {
    notify.error('Не удалось удалить документ', getApiErrorMessage(error));
  }
}

function getDocumentTypeLabel(type: string) {
  const labels: Record<string, string> = { RECEIPT: 'Приёмка', SHIPMENT: 'Отгрузка', TRANSFER: 'Перемещение', WRITE_OFF: 'Списание' };
  return labels[type] || type;
}

function getStatusLabel(status: string) {
  const labels: Record<string, string> = { DRAFT: 'Черновик', CONFIRMED: 'Подтверждён', COMPLETED: 'Завершён' };
  return labels[status] || status;
}

function formatDate(dateString: string) {
  if (!dateString) return '—';
  return new Date(dateString).toLocaleString();
}

onMounted(loadDocuments);
</script>

<style scoped>
.documents-view { display: flex; flex-direction: column; gap: 20px; }
.toolbar { display: flex; gap: 16px; align-items: center; justify-content: space-between; flex-wrap: wrap; }
.search-input { flex: 1; min-width: 300px; padding: 12px 16px; border-radius: 12px; border: 1px solid var(--border-color); background: var(--bg-card); color: var(--text-main); }
.actions { display: flex; gap: 12px; flex-wrap: wrap; }
.table-card { background: var(--bg-card); border-radius: 18px; padding: 20px; box-shadow: var(--shadow); }
.table { width: 100%; border-collapse: collapse; }
th { text-align: left; padding: 14px; color: var(--text-muted); border-bottom: 1px solid var(--border-color); }
td { padding: 14px; border-bottom: 1px solid var(--border-color); color: var(--text-main); }
th.sortable { cursor: pointer; user-select: none; }
th.sortable:hover { background: #f8fafc; }
[data-theme='light'] th.sortable:hover { background: #f1f5f9; }
.sort-icon { display: inline-block; margin-left: 4px; width: 12px; color: var(--text-muted); }
.doc-link { color: var(--accent-primary); font-weight: 700; text-decoration: none; }
.doc-link:hover { text-decoration: underline; }
.status-badge { padding: 4px 10px; border-radius: 20px; font-size: 0.85rem; font-weight: 600; }
.status-badge.draft { background: #f1f5f9; color: #475569; }
.status-badge.confirmed { background: #dcfce7; color: #15803d; }
.table-actions { display: flex; gap: 8px; }
.btn { padding: 10px 16px; border-radius: 10px; border: none; cursor: pointer; font-weight: 600; transition: opacity 0.2s; text-decoration: none; display: inline-flex; align-items: center; justify-content: center; }
.btn-primary { background: var(--accent-primary); color: white; }
.btn-secondary { background: var(--text-muted); color: white; }
.btn-info { background: #0ea5e9; color: white; }
.btn-warning { background: #f59e0b; color: white; }
.btn-success { background: #16a34a; color: white; }
.btn-danger { background: #dc2626; color: white; }
.btn-sm { padding: 6px 10px; font-size: 0.85rem; }
.empty-state { text-align: center; padding: 40px; color: var(--text-muted); }
</style>
