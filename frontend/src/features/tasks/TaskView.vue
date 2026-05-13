<template>
  <div class="page">
    <main-layout>
      <template #title>Рабочие задачи</template>
      <template #content>
        <section class="task-board">
          <div class="panel-card task-list">
            <h2>Текущие позиции на складе</h2>
            <table>
              <thead>
                <tr>
                  <th>Товар</th>
                  <th>Склад</th>
                  <th>Остаток</th>
                  <th>Действия</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in stockItems" :key="item.id">
                  <td>{{ item.product.name }}</td>
                  <td>{{ item.warehouse.name }}</td>
                  <td>{{ item.quantity }}</td>
                  <td class="actions">
                    <button @click="adjust(item.id, 1)">+ Приход</button>
                    <button @click="adjust(item.id, -1)">- Расход</button>
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-if="stockItems.length === 0" class="empty-state">Нет доступных позиций склада.</div>
          </div>

          <div class="panel-card task-form">
            <h3>Добавить позицию на склад</h3>
            <form @submit.prevent="createStock">
              <label>
                Товар
                <select v-model.number="selectedProductId">
                  <option value="0">Выберите товар</option>
                  <option v-for="product in products" :key="product.id" :value="product.id">
                    {{ product.name }} ({{ product.sku }})
                  </option>
                </select>
              </label>
              <label>
                Склад
                <select v-model.number="selectedWarehouseId">
                  <option value="0">Выберите склад</option>
                  <option v-for="warehouse in warehouses" :key="warehouse.id" :value="warehouse.id">
                    {{ warehouse.name }}
                  </option>
                </select>
              </label>
              <label>
                Количество
                <input type="number" v-model.number="quantity" min="1" />
              </label>
              <button type="submit">Добавить на склад</button>
            </form>
            <p v-if="message" class="success">{{ message }}</p>
            <p v-if="error" class="error">{{ error }}</p>
          </div>

          <div class="panel-card task-form">
            <h3>Оформление операции</h3>
            <form @submit.prevent="submitStockDocument">
              <label>
                Тип операции
                <select v-model="transactionType">
                  <option value="RECEIPT">Приход</option>
                  <option value="ISSUE">Расход</option>
                  <option value="PUTAWAY">Размещение</option>
                  <option value="PICKING">Сбор</option>
                  <option value="PACKING">Упаковка</option>
                  <option value="SHIPPING">Отгрузка</option>
                </select>
              </label>
              <label>
                Товар
                <select v-model.number="documentProductId">
                  <option value="0">Выберите товар</option>
                  <option v-for="product in products" :key="product.id" :value="product.id">
                    {{ product.name }} ({{ product.sku }})
                  </option>
                </select>
              </label>
              <label>
                Склад
                <select v-model.number="documentWarehouseId">
                  <option value="0">Выберите склад</option>
                  <option v-for="warehouse in warehouses" :key="warehouse.id" :value="warehouse.id">
                    {{ warehouse.name }}
                  </option>
                </select>
              </label>
              <label>
                Ячейка хранения
                <input v-model="documentStorageLocation" placeholder="Например, A3-12" />
              </label>
              <label>
                Количество
                <input type="number" v-model.number="documentQuantity" min="1" />
              </label>
              <label>
                Комментарий
                <textarea v-model="documentNotes" placeholder="Примечание к операции"></textarea>
              </label>
              <button type="submit">Сохранить документ</button>
            </form>
            <p v-if="docMessage" class="success">{{ docMessage }}</p>
            <p v-if="docError" class="error">{{ docError }}</p>
          </div>

          <div class="panel-card">
            <h3>Последние операционные документы</h3>
            <table>
              <thead>
                <tr>
                  <th>Операция</th>
                  <th>Товар</th>
                  <th>Склад</th>
                  <th>Кол-во</th>
                  <th>Ячейка</th>
                  <th>Дата</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="tx in transactions" :key="tx.id">
                  <td>{{ tx.transactionType }}</td>
                  <td>{{ tx.stockItem.product.name }}</td>
                  <td>{{ tx.stockItem.warehouse.name }}</td>
                  <td>{{ tx.quantity }}</td>
                  <td>{{ tx.location || '—' }}</td>
                  <td>{{ formatDate(tx.createdAt) }}</td>
                </tr>
              </tbody>
            </table>
            <div v-if="transactions.length === 0" class="empty-state">Операции не найдены.</div>
          </div>
        </section>
      </template>
    </main-layout>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import MainLayout from '../../layouts/MainLayout.vue';
import { fetchStockItems, adjustStock, createStockItem, createStockTransaction, fetchStockTransactions } from '../../api/stock';
import { fetchWarehouses } from '../../api/warehouses';
import { fetchProducts } from '../../api/products';
import type { StockItem, Warehouse, Product } from '../../types';

const stockItems = ref<StockItem[]>([]);
const warehouses = ref<Warehouse[]>([]);
const products = ref<Product[]>([]);
const transactions = ref<Array<import('../../types').StockTransaction>>([]);
const selectedWarehouseId = ref(0);
const selectedProductId = ref(0);
const quantity = ref(1);
const error = ref('');
const message = ref('');
const transactionType = ref<'RECEIPT' | 'ISSUE' | 'PUTAWAY' | 'PICKING' | 'PACKING' | 'SHIPPING'>('RECEIPT');
const documentProductId = ref(0);
const documentWarehouseId = ref(0);
const documentQuantity = ref(1);
const documentStorageLocation = ref('');
const documentNotes = ref('');
const docError = ref('');
const docMessage = ref('');

async function loadData() {
  error.value = '';
  try {
    stockItems.value = await fetchStockItems();
    warehouses.value = await fetchWarehouses();
    products.value = await fetchProducts();
    transactions.value = await fetchStockTransactions();
  } catch {
    error.value = 'Ошибка загрузки данных. Попробуйте обновить страницу.';
  }
}

async function adjust(id: number, delta: number) {
  error.value = '';
  message.value = '';
  try {
    await adjustStock(id, delta);
    message.value = delta > 0 ? 'Остаток увеличен.' : 'Остаток уменьшен.';
    await loadData();
  } catch {
    error.value = 'Не удалось изменить количество.';
  }
}

async function createStock() {
  error.value = '';
  message.value = '';

  if (!selectedProductId.value || !selectedWarehouseId.value || quantity.value <= 0) {
    error.value = 'Выберите товар, склад и введите корректное количество.';
    return;
  }

  try {
    await createStockItem({
      product: { id: selectedProductId.value },
      warehouse: { id: selectedWarehouseId.value },
      quantity: quantity.value
    });
    message.value = 'Позиция добавлена на склад.';
    quantity.value = 1;
    selectedProductId.value = 0;
    selectedWarehouseId.value = 0;
    await loadData();
  } catch {
    error.value = 'Не удалось сохранить позицию на склад.';
  }
}

async function submitStockDocument() {
  docError.value = '';
  docMessage.value = '';

  if (!documentProductId.value || !documentWarehouseId.value || documentQuantity.value <= 0) {
    docError.value = 'Заполните товар, склад и количество.';
    return;
  }

  try {
    await createStockTransaction({
      productId: documentProductId.value,
      warehouseId: documentWarehouseId.value,
      transactionType: transactionType.value,
      quantity: documentQuantity.value,
      storageLocation: documentStorageLocation.value || undefined,
      notes: documentNotes.value || undefined,
      reference: undefined
    });
    docMessage.value = 'Операция успешно зафиксирована.';
    documentQuantity.value = 1;
    documentStorageLocation.value = '';
    documentNotes.value = '';
    await loadData();
  } catch {
    docError.value = 'Не удалось сохранить документ операции.';
  }
}

onMounted(loadData);
</script>

<style scoped>
.task-board {
  display: grid;
  gap: 24px;
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
  margin-bottom: 16px;
}
th,
td {
  padding: 14px 12px;
  border-bottom: 1px solid #e2e8f0;
  text-align: left;
}
.actions {
  display: flex;
  gap: 8px;
}
button {
  padding: 10px 14px;
  border: none;
  border-radius: 12px;
  background: #2563eb;
  color: white;
  cursor: pointer;
}
button:hover {
  background: #1d4ed8;
}
.empty-state,
.success,
.error {
  margin-top: 12px;
}
.success {
  color: #16a34a;
}
.error {
  color: #b91c1c;
}
label {
  display: block;
  margin-bottom: 16px;
}
input,
select {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  margin-top: 8px;
}
</style>
