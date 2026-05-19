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

          <div class="panel-card next-steps">
            <h3>Что дальше</h3>
            <div class="step-links">
              <router-link to="/products" class="step-card">
                <span class="step-icon">📦</span>
                <div class="step-text">
                  <h4>Список товаров</h4>
                  <p>Управление номенклатурой и штрихкодами</p>
                </div>
              </router-link>
              <router-link to="/documents" class="step-card">
                <span class="step-icon">📑</span>
                <div class="step-text">
                  <h4>Складские документы</h4>
                  <p>Приёмка, отгрузка и перемещения</p>
                </div>
              </router-link>
              <router-link to="/tasks" class="step-card">
                <span class="step-icon">✅</span>
                <div class="step-text">
                  <h4>Текущие задачи</h4>
                  <p>Выполнение заданий на отбор и размещение</p>
                </div>
              </router-link>
            </div>
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

async function loadOverview() {
  try {
    overview.value = await fetchOverview();
  } catch {
    console.error('Failed to load overview');
  }
}

onMounted(loadOverview);
</script>

<style scoped>
.dashboard-panel { display: flex; flex-direction: column; gap: 20px; }
.panel-card {
  background: #ffffff;
  border-radius: 18px;
  padding: 24px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.06);
}
.welcome-card h2 { margin: 0 0 8px 0; font-size: 1.6rem; }
.welcome-card p { margin: 0; color: #64748b; }

.stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-top: 16px; }
.stat-item { padding: 24px; border-radius: 16px; background: #f8fafc; }
.stat-value { display: block; font-size: 2.2rem; font-weight: 800; color: #0f172a; }
.stat-label { color: #64748b; font-weight: 500; }
.low-stock { background: #fff7ed; }
.low-stock .stat-value { color: #ea580c; }

.step-links { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 16px; margin-top: 16px; }
.step-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 14px;
  text-decoration: none;
  color: inherit;
  transition: all 0.2s;
  border: 1px solid transparent;
}
.step-card:hover { background: white; border-color: #e2e8f0; transform: translateY(-2px); box-shadow: 0 8px 16px rgba(0,0,0,0.05); }
.step-icon { font-size: 1.8rem; background: white; width: 50px; height: 50px; display: flex; align-items: center; justify-content: center; border-radius: 10px; }
.step-text h4 { margin: 0 0 4px 0; color: #1e293b; }
.step-text p { margin: 0; font-size: 0.85rem; color: #64748b; }
</style>
