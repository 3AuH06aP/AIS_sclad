<template>
  <div class="page">
    <main-layout>
      <template #title>Карточка товара</template>
      <template #content>
        <div v-if="loading" class="panel-card empty-state">Загрузка...</div>
        <div v-else-if="error" class="panel-card error-state">{{ error }}</div>
        <div v-else-if="product" class="detail-card panel-card">
          <button type="button" class="btn-back btn btn-ghost" @click="goBack">← К списку товаров</button>
          <h2>{{ product.name }}</h2>
          <p class="meta-line">Артикул: <strong>{{ product.sku }}</strong></p>
          <p v-if="product.barcode" class="meta-line">Штрихкод: {{ product.barcode }}</p>
          <dl class="detail-grid">
            <dt>Категория</dt>
            <dd>{{ product.category || '—' }}</dd>
            <dt>Единица</dt>
            <dd>{{ product.unit || '—' }}</dd>
            <dt>Остаток (сумма по складам)</dt>
            <dd>{{ product.quantity ?? '—' }}</dd>
            <dt>Мин. остаток</dt>
            <dd>{{ product.minQuantity ?? '—' }}</dd>
            <dt v-if="product.inventoryClass">Класс</dt>
            <dd v-if="product.inventoryClass">{{ product.inventoryClass }}</dd>
            <dt v-if="product.trackingMethod">Метод учёта</dt>
            <dd v-if="product.trackingMethod">{{ product.trackingMethod }}</dd>
            <dt v-if="product.description">Описание</dt>
            <dd v-if="product.description" class="description">{{ product.description }}</dd>
          </dl>
        </div>
      </template>
    </main-layout>
  </div>
</template>

<script setup lang="ts">
import MainLayout from '../../layouts/MainLayout.vue';
import { fetchProductById } from '../../api/products';
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import type { Product } from '../../types';

const props = defineProps<{ id?: string }>();
const route = useRoute();
const router = useRouter();

const product = ref<Product | null>(null);
const loading = ref(true);
const error = ref('');

function resolveId(): number {
  const raw = props.id ?? (route.params.id as string);
  return parseInt(raw, 10);
}

async function load() {
  const id = resolveId();
  if (Number.isNaN(id)) {
    error.value = 'Некорректный идентификатор товара';
    loading.value = false;
    return;
  }
  loading.value = true;
  error.value = '';
  try {
    product.value = await fetchProductById(id);
  } catch {
    error.value = 'Товар не найден или не удалось загрузить данные.';
    product.value = null;
  } finally {
    loading.value = false;
  }
}

function goBack() {
  router.push('/products');
}

onMounted(load);
watch(
  () => route.params.id,
  () => load()
);
</script>

<style scoped>
.detail-card h2 {
  margin: 12px 0 8px 0;
}
.meta-line {
  margin: 4px 0;
  color: #64748b;
}
.detail-grid {
  display: grid;
  grid-template-columns: 160px 1fr;
  gap: 10px 20px;
  margin-top: 20px;
}
.detail-grid dt {
  color: #64748b;
  font-weight: 500;
}
.detail-grid dd {
  margin: 0;
}
.description {
  grid-column: 1 / -1;
  white-space: pre-wrap;
}
.btn-back {
  margin-bottom: 8px;
}
.error-state {
  color: #b91c1ceb;
}
.panel-card {
  background: #ffffff;
  border-radius: 18px;
  padding: 24px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.06);
}
</style>
