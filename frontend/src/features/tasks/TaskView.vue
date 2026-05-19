<template>
  <div class="page">
    <main-layout>
      <template #title>Задачи</template>
      <template #content>
        <div class="tasks-view">
          <div class="task-grid">
            <div class="panel-card tasks-header">
              <h2>Активные задачи</h2>
              <p>Здесь отображаются задачи на размещение (Putaway) и отбор (Picking) товаров.</p>
            </div>

            <div v-if="loading" class="empty-state">Загрузка задач...</div>
            <div v-else-if="myTasks.length === 0" class="panel-card empty-state">
              <div class="empty-icon">📂</div>
              <h3>Нет активных задач</h3>
              <p>Создайте приёмку или отгрузку, чтобы появились новые задачи.</p>
            </div>

            <div v-else class="task-list">
              <div v-for="task in myTasks" :key="task.id" class="panel-card task-card" :class="task.status.toLowerCase()">
                <div class="task-badge" :class="task.taskType.toLowerCase()">
                  {{ getTaskTypeLabel(task.taskType) }}
                </div>

                <div class="task-main">
                  <h3>{{ task.product.name }}</h3>
                  <div class="task-details">
                    <div class="detail-item">
                      <span class="label">Кол-во:</span>
                      <span class="value">{{ task.quantity }} {{ task.product.unit }}</span>
                    </div>
                    <div class="detail-item">
                      <span class="label">Склад:</span>
                      <span class="value">{{ task.warehouse?.name }}</span>
                    </div>
                    <div v-if="task.storageLocation" class="detail-item">
                      <span class="label">Ячейка:</span>
                      <span class="value">{{ task.storageLocation }}</span>
                    </div>
                  </div>
                  <p v-if="task.notes" class="task-notes">📝 {{ task.notes }}</p>
                </div>

                <div class="task-footer">
                  <span class="task-time">Создано: {{ formatDate(task.createdAt) }}</span>
                  <div class="task-actions">
                    <button v-if="task.status === 'PENDING'" @click="startTask(task.id)" class="btn btn-primary">Начать</button>
                    <button @click="completeTask(task.id)" class="btn btn-success">Завершить</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </main-layout>
  </div>
</template>

<script setup lang="ts">
import MainLayout from '../../layouts/MainLayout.vue';
import { fetchTasks, completeTask as apiCompleteTask, updateTask } from '../../api/tasks';
import { ref, onMounted } from 'vue';

const myTasks = ref([]);
const loading = ref(false);

async function loadTasks() {
  loading.value = true;
  try {
    // В упрощенной версии берем все задачи
    const data = await fetchTasks();
    myTasks.value = data.filter(t => t.status !== 'COMPLETED');
  } catch (error) {
    console.error('Error loading tasks:', error);
  } finally {
    loading.value = false;
  }
}

async function startTask(taskId: number) {
  try {
    const task = myTasks.value.find(t => t.id === taskId);
    if (task) {
      task.status = 'IN_PROGRESS';
      await updateTask(taskId, task);
      await loadTasks();
    }
  } catch (error) {
    console.error('Error starting task:', error);
  }
}

async function completeTask(taskId: number) {
  try {
    await apiCompleteTask(taskId);
    await loadTasks();
  } catch (error) {
    console.error('Error completing task:', error);
  }
}

function getTaskTypeLabel(type: string) {
  return type === 'PUTAWAY' ? 'Размещение' : 'Сборка';
}

function formatDate(dateString: string) {
  if (!dateString) return '—';
  return new Date(dateString).toLocaleString('ru-RU', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit' });
}

onMounted(loadTasks);
</script>

<style scoped>
.task-grid { display: flex; flex-direction: column; gap: 20px; }
.panel-card {
  background: #ffffff;
  border-radius: 18px;
  padding: 24px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.06);
}
.tasks-header h2 { margin: 0 0 8px 0; }
.tasks-header p { margin: 0; color: #64748b; }

.task-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 20px; }
.task-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: relative;
  overflow: hidden;
  border: 1px solid #f1f5f9;
}
.task-badge {
  align-self: flex-start;
  padding: 4px 12px;
  border-radius: 8px;
  font-size: 0.8rem;
  font-weight: 700;
  text-transform: uppercase;
}
.task-badge.putaway { background: #dcfce7; color: #15803d; }
.task-badge.picking { background: #e0f2fe; color: #0369a1; }

.task-main h3 { margin: 0 0 12px 0; font-size: 1.2rem; color: #0f172a; }
.task-details { display: grid; gap: 8px; }
.detail-item { display: flex; justify-content: space-between; font-size: 0.95rem; }
.detail-item .label { color: #64748b; }
.detail-item .value { font-weight: 600; color: #1e293b; }

.task-notes {
  margin: 12px 0 0 0;
  padding: 10px;
  background: #f8fafc;
  border-radius: 8px;
  font-size: 0.9rem;
  color: #475569;
}

.task-footer {
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.task-time { font-size: 0.8rem; color: #94a3b8; }

.task-actions { display: flex; gap: 8px; }
.btn {
  padding: 8px 16px;
  border-radius: 8px;
  border: none;
  font-weight: 600;
  cursor: pointer;
  font-size: 0.9rem;
}
.btn-primary { background: #2563eb; color: white; }
.btn-success { background: #16a34a; color: white; }

.empty-state { text-align: center; padding: 60px; color: #64748b; }
.empty-icon { font-size: 3rem; margin-bottom: 16px; }
</style>
