<template>
  <div class="layout-shell">
    <aside class="sidebar">
      <div class="brand">AIS Stock</div>
      <nav>
        <router-link to="/">Панель</router-link>
        <router-link to="/products">Товары</router-link>
        <router-link to="/tasks">Задачи</router-link>
        <router-link v-if="auth.role === 'admin'" to="/admin">Админка</router-link>
        <router-link v-if="auth.role === 'admin'" to="/admin/users">Пользователи</router-link>
        <router-link v-if="auth.role === 'admin'" to="/admin/logs">Журнал</router-link>
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
}
nav {
  display: grid;
  gap: 10px;
}
a {
  color: #cbd5e1;
  text-decoration: none;
  padding: 12px 14px;
  border-radius: 12px;
  display: block;
}
a.router-link-active,
a:hover {
  background: #1f2937;
  color: white;
}
.sidebar-footer {
  margin-top: auto;
}
button.ghost {
  width: 100%;
  padding: 12px 14px;
  background: transparent;
  border: 1px solid #475569;
  border-radius: 12px;
  color: #e2e8f0;
  cursor: pointer;
}
.content-area {
  background: #f8fafc;
  padding: 24px;
}
.topbar {
  margin-bottom: 20px;
}
.page-title {
  font-size: 1.4rem;
  font-weight: 700;
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
    flex-wrap: wrap;
    justify-content: space-between;
    align-items: center;
  }
  nav {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
}
</style>
