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
        <button class="ghost" @click="logout">Выйти</button>
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
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';

const auth = useAuthStore();
const router = useRouter();

function logout() {
  auth.logout();
  router.push('/login');
}
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
  background: #0f172a;
  color: #f8fafc;
}
.brand {
  font-size: 1.4rem;
  font-weight: 800;
  margin-bottom: 10px;
}
nav {
  display: grid;
  gap: 8px;
}
a {
  color: #cbd5e1;
  text-decoration: none;
  padding: 12px 14px;
  border-radius: 12px;
  display: block;
  font-weight: 500;
  transition: all 0.2s;
}
a.router-link-active,
a:hover {
  background: #1e293b;
  color: white;
}
.sidebar-footer {
  margin-top: auto;
}
button.ghost {
  width: 100%;
  padding: 12px 14px;
  background: transparent;
  border: 1px solid #334155;
  border-radius: 12px;
  color: #e2e8f0;
  cursor: pointer;
  font-weight: 600;
}
button.ghost:hover {
  background: #1e293b;
}
.content-area {
  background: #f8fafc;
  padding: 32px;
}
.topbar {
  margin-bottom: 24px;
}
.page-title {
  font-size: 1.8rem;
  font-weight: 800;
  color: #0f172a;
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
  }
}
</style>
