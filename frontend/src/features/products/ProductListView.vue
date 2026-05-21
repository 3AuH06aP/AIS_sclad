<template>
  <div class="page">
    <main-layout>
      <template #title>Товары</template>
      <template #content>
        <section class="toolbar">
          <input v-model="filter" placeholder="Поиск по названию, артикулу, штрихкоду" />
          <button @click="refresh" class="primary">Обновить</button>
          <button @click="showCreate = true" class="secondary">Добавить товар</button>
          <button @click="$refs.fileInput.click()" class="ghost">Импорт Excel</button>
          <input type="file" ref="fileInput" style="display: none" @change="handleFileUpload" accept=".xlsx, .xls" />
        </section>

        <section v-if="showCreate" class="modal-overlay" @click.self="closeCreate">
          <div class="modal-card">
            <h2>Новый товар</h2>
            <form @submit.prevent="submitNewProduct">
              <label>
                Артикул
                <input v-model="newProduct.sku" placeholder="Введите SKU" />
              </label>
              <label>
                Штрихкод
                <input v-model="newProduct.barcode" placeholder="Штрихкод" />
              </label>
              <label>
                Название
                <input v-model="newProduct.name" placeholder="Название товара" />
              </label>
              <label>
                Категория
                <input v-model="newProduct.category" list="category-options" placeholder="Категория" />
                <datalist id="category-options">
                  <option v-for="category in availableCategories" :key="category" :value="category" />
                </datalist>
              </label>
              <label>
                Единица
                <input v-model="newProduct.unit" list="unit-options" placeholder="шт, кг, упак" />
                <datalist id="unit-options">
                  <option v-for="unit in availableUnits" :key="unit" :value="unit" />
                </datalist>
              </label>
              <label>
                Класс товара
                <select v-model="newProduct.inventoryClass">
                  <option value="A">A</option>
                  <option value="B">B</option>
                  <option value="C">C</option>
                  <option value="Other">Other</option>
                </select>
              </label>
              <label>
                Метод учёта
                <select v-model="newProduct.trackingMethod">
                  <option value="FIFO">FIFO</option>
                  <option value="ABC">ABC</option>
                  <option value="Standard">Standard</option>
                </select>
              </label>
              <label>
                Мин. остаток
                <input type="number" v-model.number="newProduct.minQuantity" min="0" />
              </label>
              <label>
                Цена закупки
                <input type="number" v-model.number="newProduct.purchasePrice" min="0" step="0.01" />
              </label>
              <label>
                Цена продажи
                <input type="number" v-model.number="newProduct.salePrice" min="0" step="0.01" />
              </label>
              <label>
                Описание
                <textarea v-model="newProduct.description" placeholder="Дополнительные свойства и примечания"></textarea>
              </label>

              <div class="modal-actions">
                <button type="submit" class="primary">Сохранить</button>
                <button type="button" class="ghost" @click="closeCreate">Отмена</button>
              </div>
            </form>
            <p v-if="message" class="success">{{ message }}</p>
            <p v-if="error" class="error">{{ error }}</p>
          </div>
        </section>

        <section class="table-card">
          <table>
            <thead>
              <tr>
                <th>Артикул</th>
                <th>Штрихкод</th>
                <th>Название</th>
                <th>Категория</th>
                <th>Класс</th>
                <th>Метод</th>
                <th>Остаток</th>
                <th>Мин. остаток</th>
                <th>Ед.</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="product in filteredProducts" :key="product.id">
                <td>{{ product.sku }}</td>
                <td>{{ product.barcode || '—' }}</td>
                <td>{{ product.name }}</td>
                <td>{{ product.category }}</td>
                <td>{{ product.inventoryClass || '—' }}</td>
                <td>{{ product.trackingMethod || '—' }}</td>
                <td>{{ product.quantity }}</td>
                <td>{{ product.minQuantity }}</td>
                <td>{{ product.unit }}</td>
              </tr>
            </tbody>
          </table>
          <div v-if="loading" class="empty-state">Загрузка...</div>
          <div v-if="!loading && filteredProducts.length === 0" class="empty-state">Товары не найдены</div>
          <div v-if="error" class="error">{{ error }}</div>
        </section>
      </template>
    </main-layout>
  </div>
</template>

<script setup lang="ts">
import MainLayout from '../../layouts/MainLayout.vue';
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import { fetchProducts, createProduct, importProductsExcel } from '../../api/products';
import type { Product } from '../../types';

const route = useRoute();

const products = ref([] as Product[]);
const filter = ref('');
const loading = ref(false);
const error = ref('');
const showCreate = ref(false);
const message = ref('');
const fileInput = ref<HTMLInputElement | null>(null);

const newProduct = ref<Product>({
  id: 0,
  sku: '',
  barcode: '',
  name: '',
  description: '',
  category: '',
  unit: '',
  inventoryClass: 'A',
  trackingMethod: 'FIFO',
  quantity: 0,
  minQuantity: 0,
  purchasePrice: 0,
  salePrice: 0,
});

const availableCategories = computed(() => {
  const categories = new Set<string>();
  products.value.forEach((item) => {
    if (item.category) {
      categories.add(item.category);
    }
  });
  return Array.from(categories);
});

const availableUnits = computed(() => {
  const units = new Set<string>();
  products.value.forEach((item) => {
    if (item.unit) {
      units.add(item.unit);
    }
  });
  return Array.from(units);
});

function applyRouteQuery() {
  const q = route.query.q;
  if (typeof q === 'string' && q) {
    filter.value = q;
  }
}

async function load() {
  loading.value = true;
  error.value = '';
  try {
    products.value = await fetchProducts();
  } catch {
    error.value = 'Не удалось загрузить список товаров';
  } finally {
    loading.value = false;
  }
}

async function handleFileUpload(event: Event) {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files[0]) {
    loading.value = true;
    error.value = '';
    message.value = '';
    try {
      await importProductsExcel(target.files[0]);
      message.value = 'Товары успешно импортированы';
      await load();
    } catch (err) {
      error.value = 'Ошибка при импорте файла. Проверьте формат.';
    } finally {
      loading.value = false;
      if (fileInput.value) fileInput.value.value = '';
    }
  }
}

function resetNewProduct() {
  newProduct.value = {
    id: 0,
    sku: '',
    barcode: '',
    name: '',
    description: '',
    category: '',
    unit: '',
    inventoryClass: 'A',
    trackingMethod: 'FIFO',
    quantity: 0,
    minQuantity: 0,
    purchasePrice: 0,
    salePrice: 0,
  };
}

function closeCreate() {
  showCreate.value = false;
  error.value = '';
  message.value = '';
  resetNewProduct();
}

async function submitNewProduct() {
  error.value = '';
  message.value = '';

  if (!newProduct.value.sku.trim() || !newProduct.value.name.trim()) {
    error.value = 'Заполните артикул и название товара.';
    return;
  }

  try {
    await createProduct({
      ...newProduct.value,
      category: newProduct.value.category.trim(),
      unit: newProduct.value.unit.trim(),
      description: newProduct.value.description?.trim() || ''
    });
    message.value = 'Товар успешно добавлен.';
    await load();
    closeCreate();
  } catch {
    error.value = 'Не удалось сохранить новый товар.';
  }
}

function refresh() {
  load();
}

const filteredProducts = computed(() => {
  const query = filter.value.toLowerCase().trim();
  if (!query) {
    return products.value;
  }
  return products.value.filter((item) => {
    return (
      item.name.toLowerCase().includes(query) ||
      item.sku.toLowerCase().includes(query) ||
      (item.barcode && item.barcode.toLowerCase().includes(query)) ||
      item.category?.toLowerCase().includes(query) ||
      item.description?.toLowerCase().includes(query)
    );
  });
});

onMounted(() => {
  applyRouteQuery();
  load();
});

watch(
  () => route.query.q,
  () => applyRouteQuery()
);
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}
input {
  flex: 1;
  min-width: 220px;
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid #cbd5e1;
}
button.primary {
  padding: 12px 18px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
}
button.secondary {
  padding: 12px 18px;
  background: #1d4ed8;
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
}
button.secondary:hover {
  background: #1e40af;
}
button.ghost {
  padding: 12px 18px;
  background: transparent;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  cursor: pointer;
}
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  z-index: 50;
  overflow: auto;
}
.modal-card {
  width: min(720px, 100%);
  max-height: min(90vh, 860px);
  background: #ffffff;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.18);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.modal-card > form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow: auto;
}
.modal-card > form::-webkit-scrollbar {
  width: 10px;
}
.modal-card > form::-webkit-scrollbar-thumb {
  background: rgba(15, 23, 42, 0.2);
  border-radius: 10px;
}
.modal-card h2 {
  display: block;
  margin-bottom: 14px;
}
.modal-card input,
.modal-card textarea,
.modal-card select {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  margin-top: 8px;
}
.textarea {
  min-height: 100px;
}
.table-card {
  background: #ffffff;
  border-radius: 18px;
  padding: 20px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.06);
  overflow-x: auto;
}
table {
  width: 100%;
  border-collapse: collapse;
}
th,
td {
  padding: 14px 12px;
  text-align: left;
  border-bottom: 1px solid #e2e8f0;
}
th {
  color: #334155;
  font-weight: 700;
}
.empty-state,
.message,
.error {
  padding: 24px;
  text-align: center;
  color: #475569;
}
.success {
  color: #16a34a;
}
.error {
  color: #b91c1c;
}
</style>
