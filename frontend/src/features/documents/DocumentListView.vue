<template>
  <div class="page">
    <main-layout>
      <template #title>Документы</template>
      <template #content>
        <div class="documents-view">
          <div class="actions">
            <button @click="create('receipt')" class="btn btn-primary">Приёмка</button>
            <button @click="create('shipment')" class="btn btn-secondary">Отгрузка</button>
            <button @click="create('transfer')" class="btn btn-info">Перемещение</button>
            <button @click="create('write-off')" class="btn btn-warning">Списание</button>
          </div>
          <div class="table-card">
            <table class="table">
              <thead>
                <tr>
                  <th>Номер</th>
                  <th>Тип</th>
                  <th>Статус</th>
                  <th>Создан</th>
                  <th>Действия</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="doc in documents" :key="doc.id">
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
            <div v-if="documents.length === 0" class="empty-state">Документы не найдены</div>
          </div>
        </div>
      </template>
    </main-layout>
  </div>
</template>

<script setup lang="ts">
import MainLayout from '../../layouts/MainLayout.vue';
import { fetchDocuments, confirmDocument as apiConfirmDocument, deleteDocument as apiDeleteDocument } from '../../api/documents';
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useNotifyStore } from '../../stores/notify';
import { getApiErrorMessage } from '../../utils/apiError';

const router = useRouter();
const notify = useNotifyStore();
const documents = ref([]);
const loading = ref(false);

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
  if (!confirmed) {
    return;
  }

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
.actions { display: flex; gap: 12px; flex-wrap: wrap; }
.table-card { background: white; border-radius: 18px; padding: 20px; box-shadow: 0 12px 32px rgba(15, 23, 42, 0.06); }
.table { width: 100%; border-collapse: collapse; }
th { text-align: left; padding: 14px; color: #64748b; border-bottom: 1px solid #e2e8f0; }
td { padding: 14px; border-bottom: 1px solid #f1f5f9; }
.doc-link { color: #2563eb; font-weight: 700; text-decoration: none; }
.doc-link:hover { text-decoration: underline; }
.status-badge { padding: 4px 10px; border-radius: 20px; font-size: 0.85rem; font-weight: 600; }
.status-badge.draft { background: #f1f5f9; color: #475569; }
.status-badge.confirmed { background: #dcfce7; color: #15803d; }
.table-actions { display: flex; gap: 8px; }
.btn { padding: 10px 16px; border-radius: 10px; border: none; cursor: pointer; font-weight: 600; transition: opacity 0.2s; text-decoration: none; display: inline-flex; align-items: center; justify-content: center; }
.btn-primary { background: #2563eb; color: white; }
.btn-secondary { background: #64748b; color: white; }
.btn-success { background: #16a34a; color: white; }
.btn-danger { background: #dc2626; color: white; }
.btn-sm { padding: 6px 10px; font-size: 0.85rem; }
.empty-state { text-align: center; padding: 40px; color: #64748b; }
</style>
