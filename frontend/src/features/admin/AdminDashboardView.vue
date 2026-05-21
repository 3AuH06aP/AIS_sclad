<template>
  <div class="page">
    <main-layout>
      <template #title>Административная панель</template>
      <template #content>
        <section class="admin-grid">
          <div class="panel-card admin-welcome">
            <h2>Центр управления</h2>
            <p>Управление доступом, аудит действий и системные настройки.</p>
          </div>

          <div class="admin-cards">
            <router-link to="/admin/users" class="panel-card admin-link-card">
              <div class="card-icon users-icon">👥</div>
              <div class="card-info">
                <h3>Пользователи</h3>
                <p>Управление учетными записями и ролями</p>
              </div>
            </router-link>

            <router-link to="/admin/logs" class="panel-card admin-link-card">
              <div class="card-icon logs-icon">📜</div>
              <div class="card-info">
                <h3>Журнал администратора</h3>
                <p>Создание, сброс пароля, блокировка и удаление пользователей</p>
              </div>
            </router-link>
          </div>

          <div class="panel-card admin-stats">
            <h3>Статус системы</h3>
            <ul>
              <li>База данных: <span class="status-ok">Подключено</span></li>
              <li>Версия API: <span>0.0.1-SNAPSHOT</span></li>
              <li>Режим: <span class="tag">Разработка</span></li>
            </ul>
          </div>
        </section>
      </template>
    </main-layout>
  </div>
</template>

<script setup lang="ts">
import MainLayout from '../../layouts/MainLayout.vue';
</script>

<style scoped>
.admin-grid {
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
.admin-welcome h2 { margin: 0 0 8px 0; }
.admin-welcome p { margin: 0; color: #64748b; }

.admin-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}
.admin-link-card {
  display: flex;
  align-items: center;
  gap: 20px;
  text-decoration: none;
  color: inherit;
  transition: transform 0.2s, box-shadow 0.2s;
  border: 2px solid transparent;
}
.admin-link-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.1);
  border-color: #e2e8f0;
}
.card-icon {
  font-size: 2.5rem;
  background: #f1f5f9;
  width: 70px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
}
.card-info h3 { margin: 0 0 4px 0; color: #0f172a; }
.card-info p { margin: 0; color: #64748b; font-size: 0.9rem; }

.admin-stats ul { list-style: none; padding: 0; margin: 16px 0 0 0; }
.admin-stats li { margin-bottom: 12px; color: #475569; display: flex; justify-content: space-between; }
.status-ok { color: #16a34a; font-weight: 700; }
.tag { background: #fee2e2; color: #ef4444; padding: 2px 8px; border-radius: 6px; font-size: 0.8rem; font-weight: 700; }
</style>
