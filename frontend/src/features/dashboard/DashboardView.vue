<template>
  <div class="page">
    <main-layout>
      <template #title>Панель управления</template>
      <template #content>
        <section class="dashboard-panel">
          <div class="panel-card welcome-card">
            <h2>Добро пожаловать, {{ auth.user || 'пользователь' }}!</h2>
            <p>Складская система готова к работе. Ниже — ключевые показатели и состояние запасов.</p>
          </div>

          <div class="panel-card stats-card">
            <h3>Ключевые показатели</h3>
            <div class="stats-grid">
              <div class="stat-item">
                <span class="stat-value">{{ overview.products }}</span>
                <span class="stat-label">Товаров</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ overview.warehouses }}</span>
                <span class="stat-label">Складов</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ overview.stockItems }}</span>
                <span class="stat-label">Позиции на складе</span>
              </div>
              <div class="stat-item low-stock">
                <span class="stat-value">{{ overview.lowStockItems }}</span>
                <span class="stat-label">Низкий остаток</span>
              </div>
            </div>
          </div>

          <div class="panel-card">
            <h3>Что дальше</h3>
            <ul>
              <li>Перейдите к списку товаров</li>
              <li>Добавьте новые позиции товаров</li>
              <li>Добавьте складские перемещения и приемку</li>
            </ul>
          </div>
        </section>
      </template>
    </main-layout>
  </div>
</template>

<script setup lang="ts">
import MainLayout from '../../layouts/MainLayout.vue';
import { useAuthStore } from '../../stores/auth';
import { fetchOverview } from '../../api/overview';
import { ref, onMounted } from 'vue';

const auth = useAuthStore();
const overview = ref({ products: 0, warehouses: 0, stockItems: 0, lowStockItems: 0 });
const error = ref('');

async function loadOverview() {
  error.value = '';
  try {
    overview.value = await fetchOverview();
  } catch {
    error.value = 'Не удалось загрузить ключевые показатели.';
  }
}

onMounted(loadOverview);
</script>

<style scoped>
.page {
  padding: 24px;
}
.dashboard-panel {
  display: grid;
  gap: 20px;
}
.panel-card {
  background: #ffffff;
  border-radius: 18px;
  padding: 24px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.06);
}
.welcome-card p {
  margin: 0;
  color: #475569;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 16px;
}
.stat-item {
  padding: 18px;
  border-radius: 16px;
  background: #f8fafc;
}
.stat-value {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
}
.stat-label {
  color: #64748b;
}
.low-stock {
  background: #ffedd5;
}
h2,
h3 {
  margin-top: 0;
}
</style>
