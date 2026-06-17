<template>
  <div class="layout-shell">
    <aside class="sidebar">
      <div class="brand">AIS Stock</div>
      <nav>
        <router-link to="/">Панель</router-link>
        <router-link to="/products">Товары</router-link>
        <router-link to="/documents">Документы</router-link>
        <router-link to="/reports">Остатки</router-link>
        <router-link to="/reports/movements">Движения</router-link>
        <router-link to="/tasks">Задачи</router-link>
        <router-link v-if="auth.role === 'admin'" to="/admin">Администрирование</router-link>
      </nav>

      <div class="sidebar-footer">
        <!-- Переключатель тем -->
        <button class="theme-toggle" @click="toggleTheme" :title="currentTheme === 'dark' ? 'Включить светлую тему' : 'Включить стандартную тему'">
          <span v-if="currentTheme === 'dark'">☀️ Светлая тема</span>
          <span v-else>🌙 Темная тема</span>
        </button>

        <button class="ghost logout-btn" @click="logout">Выйти</button>
      </div>
    </aside>
    <main class="content-area">
      <header class="topbar">
        <div class="page-title"><slot name="title" /></div>
      </header>
      <section class="page-body">
        <slot name="content" />
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';

const auth = useAuthStore();
const router = useRouter();
const currentTheme = ref('dark');

function toggleTheme() {
  currentTheme.value = currentTheme.value === 'dark' ? 'light' : 'dark';
  applyTheme();
}

function applyTheme() {
  document.documentElement.setAttribute('data-theme', currentTheme.value);
  localStorage.setItem('ais-stock-theme', currentTheme.value);
}

function logout() {
  auth.logout();
  router.push('/login');
}

onMounted(() => {
  const saved = localStorage.getItem('ais-stock-theme');
  if (saved) {
    currentTheme.value = saved;
  }
  applyTheme();
});
</script>

<style scoped>
.layout-shell {
  display: grid;
  grid-template-columns: 260px 1fr;
  min-height: 100vh;
}
.sidebar {
  display: flex;
  flex-direction: column;
  padding: 28px 18px;
  gap: 24px;
  background: var(--bg-sidebar);
  color: var(--text-sidebar);
  transition: background-color 0.3s ease;
  border-right: 1px solid var(--border-color);
}
.brand {
  font-size: 1.4rem;
  font-weight: 800;
  margin-bottom: 10px;
  color: white;
}
[data-theme='light'] .brand {
  color: var(--accent-primary);
}
nav {
  display: grid;
  gap: 8px;
}
a {
  color: var(--text-sidebar);
  text-decoration: none;
  padding: 12px 14px;
  border-radius: 12px;
  display: block;
  font-weight: 500;
  transition: all 0.2s;
}
a.router-link-active,
a:hover {
  background: var(--sidebar-active);
  color: white;
}
[data-theme='light'] a.router-link-active,
[data-theme='light'] a:hover {
  color: var(--accent-primary);
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.sidebar-footer {
  margin-top: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.theme-toggle {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: white;
  padding: 10px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 0.85rem;
  font-weight: 600;
  transition: all 0.2s;
}
[data-theme='light'] .theme-toggle {
  background: white;
  border-color: var(--border-color);
  color: var(--text-main);
}
.theme-toggle:hover {
  background: rgba(255, 255, 255, 0.1);
}

button.ghost {
  width: 100%;
  padding: 12px 14px;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: #e2e8f0;
  cursor: pointer;
  font-weight: 600;
}
[data-theme='light'] button.ghost {
  border-color: var(--border-color);
  color: var(--text-main);
}
button.ghost:hover {
  background: var(--sidebar-active);
}

.content-area {
  background: var(--bg-app);
  padding: 32px;
  transition: background-color 0.3s ease;
}
.topbar {
  margin-bottom: 24px;
}
.page-title {
  font-size: 1.8rem;
  font-weight: 800;
  color: var(--text-main);
}
.page-body {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
@media (max-width: 1024px) {
  .layout-shell {
    grid-template-columns: 1fr;
  }
  .sidebar {
    flex-direction: row;
    padding: 16px;
    border-right: none;
    border-bottom: 1px solid var(--border-color);
  }
}
</style>
