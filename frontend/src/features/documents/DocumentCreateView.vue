<template>
  <div class="page">
    <main-layout>
      <template #title>{{ title }}</template>
      <template #content>
        <div class="document-create">
          <div class="table-card">
            <form @submit.prevent="submitDocument">
              <div class="form-grid">
                <div class="form-group">
                  <label>Номер документа</label>
                  <input v-model="document.documentNumber" placeholder="Автогенерация" />
                </div>

                <div v-if="showWarehouseTo" class="form-group">
                  <label>Склад назначения</label>
                  <select v-model="selectedWarehouseToId" required>
                    <option :value="null" disabled>Выберите склад</option>
                    <option v-for="wh in warehouses" :key="wh.id" :value="wh.id">{{ wh.name }}</option>
                  </select>
                </div>

                <div v-if="showWarehouseFrom" class="form-group">
                  <label>Склад отправления</label>
                  <select v-model="selectedWarehouseFromId" required>
                    <option :value="null" disabled>Выберите склад</option>
                    <option v-for="wh in warehouses" :key="wh.id" :value="wh.id">{{ wh.name }}</option>
                  </select>
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
                      <th></th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(item, index) in document.items" :key="index">
                      <td>
                        <select v-model="item.product.id" required>
                          <option :value="null" disabled>Выбрать товар</option>
                          <option v-for="p in products" :key="p.id" :value="p.id">{{ p.name }} ({{ p.sku }})</option>
                        </select>
                      </td>
                      <td><input type="number" v-model.number="item.quantity" min="1" required /></td>
                      <td v-if="showWarehouseTo"><input v-model="item.storageLocation" placeholder="Ячейка" /></td>
                      <td><input v-model="item.batch" placeholder="Партия" /></td>
                      <td><button type="button" @click="removeItem(index)" class="btn-icon">×</button></td>
                    </tr>
                  </tbody>
                </table>
                <button type="button" @click="addItem" class="btn btn-secondary">Добавить строку</button>
              </div>

              <div class="actions">
                <button type="submit" class="btn btn-primary" :disabled="loading">Создать черновик</button>
                <button type="button" @click="$router.push('/documents')" class="btn btn-ghost">Отмена</button>
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
import {
  createReceiptDocument,
  createShipmentDocument,
  createTransferDocument,
  createWriteOffDocument
} from '../../api/documents';
import { useNotifyStore } from '../../stores/notify';
import { getApiErrorMessage } from '../../utils/apiError';

const notify = useNotifyStore();

const route = useRoute();
const router = useRouter();

const type = route.params.type as string;
const loading = ref(false);

const title = computed(() => {
  if (type === 'receipt') return 'Новая приёмка';
  if (type === 'shipment') return 'Новая отгрузка';
  if (type === 'transfer') return 'Новое перемещение';
  if (type === 'write-off') return 'Новое списание';
  return 'Новый документ';
});

const showWarehouseTo = computed(() => ['receipt', 'transfer'].includes(type));
const showWarehouseFrom = computed(() => ['shipment', 'transfer', 'write-off'].includes(type));

const warehouses = ref([]);
const products = ref([]);
const selectedWarehouseToId = ref(null);
const selectedWarehouseFromId = ref(null);
const document = ref({
  documentNumber: '',
  items: []
});

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

async function submitDocument() {
  if (document.value.items.length === 0) {
    notify.warning('Пустой документ', 'Добавьте хотя бы одну строку с товаром.');
    return;
  }

  if (showWarehouseTo.value && !selectedWarehouseToId.value) {
    notify.warning('Не выбран склад', 'Укажите склад назначения.');
    return;
  }

  if (showWarehouseFrom.value && !selectedWarehouseFromId.value) {
    notify.warning('Не выбран склад', 'Укажите склад отправления.');
    return;
  }

  loading.value = true;
  try {
    const docData = { ...document.value };
    if (showWarehouseTo.value) (docData as any).warehouseTo = { id: selectedWarehouseToId.value };
    if (showWarehouseFrom.value) (docData as any).warehouseFrom = { id: selectedWarehouseFromId.value };

    if (type === 'receipt') await createReceiptDocument(docData);
    else if (type === 'shipment') await createShipmentDocument(docData);
    else if (type === 'transfer') await createTransferDocument(docData);
    else if (type === 'write-off') await createWriteOffDocument(docData);

    notify.success('Черновик создан', 'Документ сохранён и доступен в списке.');
    router.push('/documents');
  } catch (error) {
    notify.error(
      'Не удалось создать документ',
      getApiErrorMessage(error, 'Проверьте склад, товары и количество.')
    );
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  try {
    const [pData, wData] = await Promise.all([fetchProducts(), fetchWarehouses()]);
    products.value = pData;
    warehouses.value = wData;
    addItem();
  } catch (err) {
    notify.error('Не удалось загрузить форму', getApiErrorMessage(err));
  }
});
</script>

<style scoped>
.table-card {
  background: white;
  border-radius: 18px;
  padding: 28px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.06);
}
.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}
.form-group { display: flex; flex-direction: column; gap: 8px; }
label { font-weight: 600; color: #475569; }
input, select {
  padding: 12px;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  font-size: 1rem;
}
.items-section { margin-top: 20px; }
.table { width: 100%; border-collapse: collapse; margin-bottom: 16px; }
th { text-align: left; padding: 12px; color: #64748b; border-bottom: 1px solid #e2e8f0; }
td { padding: 12px; border-bottom: 1px solid #f1f5f9; }
.btn-icon {
  background: #fee2e2;
  color: #ef4444;
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  justify-content: center;
}
.actions { display: flex; gap: 12px; margin-top: 30px; }
.btn { padding: 12px 24px; border-radius: 10px; font-weight: 600; cursor: pointer; border: none; transition: background 0.2s; }
.btn-primary { background: #2563eb; color: white; }
.btn-primary:disabled { background: #94a3b8; cursor: not-allowed; }
.btn-secondary { background: #f1f5f9; color: #475569; }
.btn-ghost { background: transparent; border: 1px solid #cbd5e1; }
</style>
