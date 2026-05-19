<template>
  <div class="page">
    <main-layout>
      <template #title>{{ isEdit ? 'Редактирование документа' : 'Просмотр документа' }} #{{ docId }}</template>
      <template #content>
        <div class="document-card">
          <div class="table-card">
            <div v-if="loading" class="loading-state">Загрузка данных...</div>
            <form v-else @submit.prevent="save">
              <div class="doc-header-info">
                <div class="badge-row">
                  <span :class="['status-badge', document.status?.toLowerCase()]">{{ getStatusLabel(document.status) }}</span>
                  <span class="type-badge">{{ getDocumentTypeLabel(document.documentType) }}</span>
                </div>
                <div class="form-grid">
                  <div class="form-group">
                    <label>Номер документа</label>
                    <input v-model="document.documentNumber" :disabled="!isEditable" />
                  </div>
                  <div v-if="showWarehouseTo" class="form-group">
                    <label>Склад назначения</label>
                    <select v-model="document.warehouseTo.id" :disabled="!isEditable" required>
                      <option v-for="wh in warehouses" :key="wh.id" :value="wh.id">{{ wh.name }}</option>
                    </select>
                  </div>
                  <div v-if="showWarehouseFrom" class="form-group">
                    <label>Склад отправления</label>
                    <select v-model="document.warehouseFrom.id" :disabled="!isEditable" required>
                      <option v-for="wh in warehouses" :key="wh.id" :value="wh.id">{{ wh.name }}</option>
                    </select>
                  </div>
                </div>
              </div>

              <div class="items-section">
                <h3>Товары</h3>
                <table class="table">
                  <thead>
                    <tr>
                      <th>Товар</th>
                      <th>Количество</th>
                      <th v-if="showWarehouseTo">Ячейка</th>
                      <th>Партия</th>
                      <th v-if="isEditable"></th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(item, index) in document.items" :key="index">
                      <td>
                        <select v-model="item.product.id" :disabled="!isEditable" required>
                          <option v-for="p in products" :key="p.id" :value="p.id">{{ p.name }} ({{ p.sku }})</option>
                        </select>
                      </td>
                      <td><input type="number" v-model.number="item.quantity" min="1" :disabled="!isEditable" required /></td>
                      <td v-if="showWarehouseTo"><input v-model="item.storageLocation" placeholder="Ячейка" :disabled="!isEditable" /></td>
                      <td><input v-model="item.batch" placeholder="Партия" :disabled="!isEditable" /></td>
                      <td v-if="isEditable"><button type="button" @click="removeItem(index)" class="btn-icon">×</button></td>
                    </tr>
                  </tbody>
                </table>
                <button v-if="isEditable" type="button" @click="addItem" class="btn btn-secondary">Добавить строку</button>
              </div>

              <div class="doc-footer-notes">
                <label>Примечания</label>
                <textarea v-model="document.notes" :disabled="!isEditable" placeholder="Дополнительная информация..."></textarea>
              </div>

              <div class="actions">
                <button v-if="isEditable" type="submit" class="btn btn-primary">Сохранить изменения</button>
                <button v-if="document.status === 'DRAFT'" type="button" @click="confirmDoc" class="btn btn-success">Подтвердить (Провести)</button>
                <button type="button" @click="$router.push('/documents')" class="btn btn-ghost">Назад к списку</button>
              </div>
            </form>
          </div>
        </div>
      </template>
    </main-layout>
  </div>
</template>

<script setup lang="ts">
import MainLayout from '../../layouts/MainLayout.vue';
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchProducts } from '../../api/products';
import { fetchWarehouses } from '../../api/warehouses';
import { fetchDocumentById, updateDocument, confirmDocument } from '../../api/documents';
import { useNotifyStore } from '../../stores/notify';
import { getApiErrorMessage } from '../../utils/apiError';

const notify = useNotifyStore();

const route = useRoute();
const router = useRouter();
const docId = Number(route.params.id);

const loading = ref(true);
const warehouses = ref([]);
const products = ref([]);
const document = ref({
  id: null,
  documentType: '',
  documentNumber: '',
  status: '',
  warehouseTo: { id: null },
  warehouseFrom: { id: null },
  items: [],
  notes: ''
});

const isEdit = computed(() => document.value.status === 'DRAFT');
const isEditable = computed(() => document.value.status === 'DRAFT');

const showWarehouseTo = computed(() => ['RECEIPT', 'TRANSFER'].includes(document.value.documentType));
const showWarehouseFrom = computed(() => ['SHIPMENT', 'TRANSFER', 'WRITE_OFF'].includes(document.value.documentType));

async function loadData() {
  loading.value = true;
  try {
    const [pData, wData, dData] = await Promise.all([
      fetchProducts(),
      fetchWarehouses(),
      fetchDocumentById(docId)
    ]);
    products.value = pData;
    warehouses.value = wData;

    // Normalize nested objects for binding
    if (!dData.warehouseTo) dData.warehouseTo = { id: null };
    if (!dData.warehouseFrom) dData.warehouseFrom = { id: null };
    document.value = dData;
  } catch (error) {
    notify.error('Не удалось открыть документ', getApiErrorMessage(error));
    router.push('/documents');
  } finally {
    loading.value = false;
  }
}

function addItem() {
  document.value.items.push({
    product: { id: null },
    quantity: 1,
    storageLocation: '',
    batch: ''
  });
}

function removeItem(index: number) {
  document.value.items.splice(index, 1);
}

async function save() {
  try {
    await updateDocument(docId, document.value);
    notify.success('Изменения сохранены');
    await loadData();
  } catch (error) {
    notify.error('Не удалось сохранить документ', getApiErrorMessage(error));
  }
}

async function confirmDoc() {
  try {
    await confirmDocument(docId);
    notify.success('Документ проведён', 'Остатки на складе обновлены.');
    await loadData();
  } catch (error) {
    notify.error('Не удалось провести документ', getApiErrorMessage(error, 'Ошибка при подтверждении'));
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

onMounted(loadData);
</script>

<style scoped>
.table-card { background: white; border-radius: 18px; padding: 28px; box-shadow: 0 12px 32px rgba(15, 23, 42, 0.06); }
.badge-row { display: flex; gap: 10px; margin-bottom: 20px; }
.status-badge { padding: 6px 14px; border-radius: 20px; font-weight: 700; font-size: 0.85rem; }
.status-badge.draft { background: #f1f5f9; color: #475569; }
.status-badge.confirmed { background: #dcfce7; color: #15803d; }
.type-badge { background: #e0f2fe; color: #0369a1; padding: 6px 14px; border-radius: 20px; font-weight: 700; font-size: 0.85rem; }

.form-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 20px; }
.form-group { display: flex; flex-direction: column; gap: 8px; }
label { font-weight: 600; color: #475569; font-size: 0.9rem; }
input, select, textarea { padding: 12px; border: 1px solid #cbd5e1; border-radius: 10px; font-size: 1rem; }
textarea { min-height: 80px; resize: vertical; }

.items-section { margin-top: 30px; }
.table { width: 100%; border-collapse: collapse; margin-bottom: 16px; }
th { text-align: left; padding: 12px; color: #64748b; border-bottom: 1px solid #e2e8f0; }
td { padding: 12px; border-bottom: 1px solid #f1f5f9; }

.actions { display: flex; gap: 12px; margin-top: 40px; flex-wrap: wrap; }
.btn { padding: 12px 24px; border-radius: 10px; font-weight: 600; cursor: pointer; border: none; }
.btn-primary { background: #2563eb; color: white; }
.btn-success { background: #16a34a; color: white; }
.btn-ghost { background: transparent; border: 1px solid #cbd5e1; }
.btn-icon { background: #fee2e2; color: #ef4444; border: none; width: 32px; height: 32px; border-radius: 8px; cursor: pointer; }

.loading-state { text-align: center; padding: 40px; color: #64748b; }
</style>
