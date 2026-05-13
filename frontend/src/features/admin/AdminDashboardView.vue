<template>
  <div class="page">
    <main-layout>
      <template #title>Административная панель</template>
      <template #content>
        <section class="dashboard-panel admin-dashboard">
          <div class="panel-card">
            <h2>Админка AIS Stock</h2>
            <p>Здесь вы управляете пользователями, ролями и получаете доступ к административным инструментам.</p>
          </div>

          <div class="panel-card admin-notice">
            <h3>Рекомендуем</h3>
            <ul>
              <li>Создавать новых пользователей и администраторов</li>
              <li>Проверять остатки и статусы складов</li>
              <li>Управлять доступом через отдельную админскую панель</li>
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
.admin-notice ul {
  margin: 0;
  padding-left: 20px;
}
</style>
