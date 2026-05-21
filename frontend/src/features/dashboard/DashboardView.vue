<template>
  <div class="page">
    <main-layout>
      <template #title>Панель управления</template>
      <template #content>
        <section class="dashboard-panel">
          <div class="panel-card welcome-card">
            <h2>Здравствуйте, {{ displayName }} ({{ roleLabel }})!</h2>
            <p v-if="auth.lastLoginAt" class="muted">
              Дата и время предыдущего входа:
              {{ formatLastLogin(auth.lastLoginAt) }}
            </p>
            <p v-else class="muted">Это ваш первый вход в систему (или данные о прошлом входе ещё не сохранялись).</p>
            <p class="lead">
              Складская система готова к работе. Ниже — ключевые показатели, быстрые действия и предупреждения по
              остаткам.
            </p>
          </div>

          <div class="panel-card stats-card">
            <h3>Ключевые показатели</h3>
            <p class="kpi-note muted">
              Приход/расход — число складских транзакций (движений) за период в UTC; при перемещениях возможны и приход, и
              расход в один день.
            </p>
            <div class="stats-grid">
              <div class="stat-item">
                <span class="stat-value">{{ formatInt(overview.totalStockQuantity) }}</span>
                <span class="stat-label">Всего на складе, шт.</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ formatInt(overview.productLinesCount) }}</span>
                <span class="stat-label">Товарных позиций (номенклатура)</span>
              </div>
              <div class="stat-item sub-kpi">
                <span class="stat-sub">Сегодня: {{ overview.receiptsToday }}</span>
                <span class="stat-sub">Неделя: {{ overview.receiptsWeek }}</span>
                <span class="stat-label">Приходов (операции)</span>
              </div>
              <div class="stat-item sub-kpi">
                <span class="stat-sub">Сегодня: {{ overview.issuesToday }}</span>
                <span class="stat-sub">Неделя: {{ overview.issuesWeek }}</span>
                <span class="stat-label">Расходов (операции)</span>
              </div>
              <div class="stat-item muted-tile">
                <span class="stat-value small">{{ overview.warehousesCount }}</span>
                <span class="stat-label">Складов</span>
              </div>
              <div class="stat-item low-stock">
                <span class="stat-value">{{ overview.lowStockCount }}</span>
                <span class="stat-label">Позиций с остатком ≤ мин.</span>
              </div>
            </div>
          </div>

          <div class="panel-card quick-actions">
            <h3>Быстрые действия</h3>
            <div class="action-row">
              <router-link to="/documents/create/receipt" class="action-btn primary-action">Оформить приход</router-link>
              <router-link to="/documents/create/shipment" class="action-btn primary-action">Оформить расход</router-link>
              <router-link to="/reports" class="action-btn secondary-action">Посмотреть остатки</router-link>
            </div>
          </div>

          <div class="panel-card search-card">
            <h3>Быстрый поиск товара</h3>
            <p class="muted">По артикулу или названию. Точное совпадение артикула или один результат — откроется карточка.</p>
            <div class="search-row">
              <input v-model="searchQuery" type="search" placeholder="Артикул или название..." @keydown.enter.prevent="runQuickSearch" />
              <button type="button" class="btn-search" :disabled="searching" @click="runQuickSearch">
                {{ searching ? 'Поиск…' : 'Найти' }}
              </button>
            </div>
          </div>

          <div class="panel-card warnings-card">
            <h3>Предупреждения</h3>
            <div v-if="overview.lowStockProducts.length" class="low-list">
              <p class="muted">Товары с остатком не выше минимального:</p>
              <ul>
                <li v-for="item in overview.lowStockProducts" :key="item.productId">
                  <router-link :to="'/products/' + item.productId" class="low-link">
                    {{ item.name }} ({{ item.sku }}) — остаток {{ item.quantity }}, мин. {{ item.minQuantity }}
                  </router-link>
                </li>
              </ul>
            </div>
            <p v-else class="muted">Товаров ниже минимального остатка не найдено.</p>
            <div class="tasks-line">
              <span v-if="pendingTaskCount > 0" class="task-badge-block">
                Невыполненных задач:
                <router-link to="/tasks" class="task-link">{{ pendingTaskCount }}</router-link>
              </span>
              <span v-else class="muted">Невыполненных задач нет.</span>
            </div>
          </div>

          <div class="panel-card chart-card">
            <h3>Движение за последние 7 дней</h3>
            <p class="muted">Сумма количеств по складским транзакциям (шт.), интервал дат в UTC.</p>
            <div class="chart-wrap">
              <canvas ref="chartCanvas"></canvas>
            </div>
          </div>

          <div class="panel-card next-steps">
            <h3>Ещё разделы</h3>
            <div class="step-links">
              <router-link to="/products" class="step-card">
                <span class="step-icon">📦</span>
                <div class="step-text">
                  <h4>Список товаров</h4>
                  <p>Номенклатура и фильтры</p>
                </div>
              </router-link>
              <router-link to="/documents" class="step-card">
                <span class="step-icon">📑</span>
                <div class="step-text">
                  <h4>Складские документы</h4>
                  <p>Все приходы и расходы</p>
                </div>
              </router-link>
              <router-link to="/reports/movements" class="step-card">
                <span class="step-icon">📊</span>
                <div class="step-text">
                  <h4>Отчёт по движениям</h4>
                  <p>Детализация по периоду</p>
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
import type { Overview } from '../../api/overview';
import { searchProductsSummary } from '../../api/products';
import { fetchTasks } from '../../api/tasks';
import type { UserRole } from '../../types';
import api from '../../api/http';
import Chart from 'chart.js/auto';
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

const auth = useAuthStore();
const router = useRouter();

const ROLE_LABELS: Record<UserRole, string> = {
  admin: 'Администратор',
  storekeeper: 'Кладовщик',
  user: 'Пользователь'
};

const displayName = computed(() => (auth.fullName ? auth.fullName : auth.user));
const roleLabel = computed(() => ROLE_LABELS[auth.role] || auth.role);

const overview = ref<Overview>({
  productLinesCount: 0,
  warehousesCount: 0,
  totalStockQuantity: 0,
  lowStockCount: 0,
  lowStockProducts: [],
  receiptsToday: 0,
  receiptsWeek: 0,
  issuesToday: 0,
  issuesWeek: 0
});

const searchQuery = ref('');
const searching = ref(false);
const pendingTaskCount = ref(0);
const chartCanvas = ref<HTMLCanvasElement | null>(null);
let movementChart: Chart | null = null;

function formatInt(n: number) {
  return new Intl.NumberFormat('ru-RU').format(n);
}

function formatLastLogin(iso: string) {
  try {
    const d = new Date(iso);
    return new Intl.DateTimeFormat('ru-RU', {
      dateStyle: 'medium',
      timeStyle: 'short'
    }).format(d);
  } catch {
    return iso;
  }
}

async function loadOverview() {
  try {
    overview.value = await fetchOverview();
  } catch {
    console.error('Failed to load overview');
  }
}

async function loadTaskCount() {
  try {
    const tasks = await fetchTasks();
    pendingTaskCount.value = tasks.filter((t) => t.status !== 'COMPLETED').length;
  } catch {
    pendingTaskCount.value = 0;
  }
}

function utcYmd(d: Date) {
  return d.toISOString().slice(0, 10);
}

function last7UtcDateStrings(): string[] {
  const days: string[] = [];
  const now = new Date();
  const end = Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), now.getUTCDate());
  for (let i = 6; i >= 0; i--) {
    const t = new Date(end - i * 86400000);
    days.push(utcYmd(t));
  }
  return days;
}

interface MovementApiRow {
  transactionDate: string;
  type: 'IN' | 'OUT';
  quantity: number;
}

async function loadMovementChart() {
  const dayOrder = last7UtcDateStrings();
  const from = dayOrder[0];
  const to = dayOrder[dayOrder.length - 1];
  let rows: MovementApiRow[] = [];
  try {
    const response = await api.get<MovementApiRow[]>('/reports/transactions', {
      params: { from, to }
    });
    rows = response.data;
  } catch {
    rows = [];
  }

  const inByDay = new Map<string, number>();
  const outByDay = new Map<string, number>();
  for (const d of dayOrder) {
    inByDay.set(d, 0);
    outByDay.set(d, 0);
  }
  for (const row of rows) {
    const key = row.transactionDate.slice(0, 10);
    if (!inByDay.has(key)) {
      continue;
    }
    const qty = row.quantity ?? 0;
    if (row.type === 'IN') {
      inByDay.set(key, (inByDay.get(key) || 0) + qty);
    } else {
      outByDay.set(key, (outByDay.get(key) || 0) + qty);
    }
  }

  const labels = dayOrder.map((d) => {
    const [y, m, day] = d.split('-');
    return `${day}.${m}`;
  });
  const dataIn = dayOrder.map((d) => inByDay.get(d) || 0);
  const dataOut = dayOrder.map((d) => outByDay.get(d) || 0);

  if (movementChart) {
    movementChart.destroy();
    movementChart = null;
  }
  const canvas = chartCanvas.value;
  if (!canvas) {
    return;
  }
  movementChart = new Chart(canvas.getContext('2d')!, {
    type: 'bar',
    data: {
      labels,
      datasets: [
        {
          label: 'Приход, шт.',
          data: dataIn,
          backgroundColor: 'rgba(34, 197, 94, 0.7)'
        },
        {
          label: 'Расход, шт.',
          data: dataOut,
          backgroundColor: 'rgba(239, 68, 68, 0.7)'
        }
      ]
    },
    options: {
      responsive: true,
      scales: {
        x: { stacked: false },
        y: { beginAtZero: true }
      }
    }
  });
}

async function runQuickSearch() {
  const q = searchQuery.value.trim();
  if (!q) {
    return;
  }
  searching.value = true;
  try {
    const results = await searchProductsSummary(q);
    if (results.length === 0) {
      router.push({ path: '/products', query: { q } });
      return;
    }
    const exactSku = results.find((p) => p.sku.toLowerCase() === q.toLowerCase());
    if (exactSku) {
      router.push('/products/' + exactSku.id);
      return;
    }
    if (results.length === 1) {
      router.push('/products/' + results[0].id);
      return;
    }
    router.push({ path: '/products', query: { q } });
  } finally {
    searching.value = false;
  }
}

onMounted(async () => {
  await Promise.all([loadOverview(), loadTaskCount()]);
  await nextTick();
  await loadMovementChart();
});

onBeforeUnmount(() => {
  if (movementChart) {
    movementChart.destroy();
    movementChart = null;
  }
});
</script>

<style scoped>
.dashboard-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.panel-card {
  background: #ffffff;
  border-radius: 18px;
  padding: 24px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.06);
}
.welcome-card h2 {
  margin: 0 0 8px 0;
  font-size: 1.55rem;
}
.lead,
.welcome-card p {
  margin: 8px 0 0 0;
  color: #64748b;
}
.muted {
  color: #64748b;
  font-size: 0.92rem;
}
.kpi-note {
  margin: 0 0 12px 0;
  font-size: 0.85rem;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 8px;
}
.stat-item {
  padding: 24px;
  border-radius: 16px;
  background: #f8fafc;
}
.stat-item.sub-kpi {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.stat-sub {
  font-size: 1.05rem;
  font-weight: 600;
  color: #1e293b;
}
.stat-value {
  display: block;
  font-size: 2.2rem;
  font-weight: 800;
  color: #0f172a;
}
.stat-value.small {
  font-size: 1.6rem;
}
.stat-label {
  color: #64748b;
  font-weight: 500;
  font-size: 0.9rem;
}
.muted-tile {
  opacity: 0.95;
}
.low-stock {
  background: #fff7ed;
}
.low-stock .stat-value {
  color: #ea580c;
}

.quick-actions .action-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 12px;
}
.action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 14px 22px;
  border-radius: 12px;
  font-weight: 600;
  text-decoration: none;
  border: 1px solid transparent;
  transition:
    transform 0.15s,
    box-shadow 0.15s;
}
.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
}
.primary-action {
  background: #2563eb;
  color: white;
}
.secondary-action {
  background: #f1f5f9;
  color: #0f172a;
  border-color: #e2e8f0;
}

.search-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 12px;
}
.search-row input {
  flex: 1;
  min-width: 220px;
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid #cbd5e1;
}
.btn-search {
  padding: 12px 22px;
  border-radius: 12px;
  border: none;
  background: #0f172a;
  color: white;
  font-weight: 600;
  cursor: pointer;
}
.btn-search:disabled {
  opacity: 0.6;
  cursor: wait;
}

.low-list ul {
  margin: 8px 0 0 0;
  padding-left: 1.1rem;
}
.low-link {
  color: #c2410c;
  font-weight: 500;
}
.tasks-line {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #e2e8f0;
}
.task-link {
  font-weight: 700;
  color: #2563eb;
  margin-left: 4px;
}

.chart-wrap {
  position: relative;
  width: 100%;
  max-width: 720px;
  margin-top: 16px;
  min-height: 260px;
}

.step-links {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
  margin-top: 16px;
}
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
.step-card:hover {
  background: white;
  border-color: #e2e8f0;
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.05);
}
.step-icon {
  font-size: 1.8rem;
  background: white;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
}
.step-text h4 {
  margin: 0 0 4px 0;
  color: #1e293b;
}
.step-text p {
  margin: 0;
  font-size: 0.85rem;
  color: #64748b;
}
</style>
