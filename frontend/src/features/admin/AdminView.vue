<template>
  <div class="page">
    <main-layout>
      <template #title>Администрирование</template>
      <template #content>
        <div class="admin-view">
          <div class="tabs">
            <button 
              :class="['tab', { active: activeTab === 'users' }]"
              @click="activeTab = 'users'"
            >
              Пользователи
            </button>
            <button 
              :class="['tab', { active: activeTab === 'logs' }]"
              @click="activeTab = 'logs'"
            >
              Журнал
            </button>
          </div>

          <!-- Users Tab -->
          <div v-if="activeTab === 'users'" class="tab-content">
            <section class="user-management">
              <div class="panel-card">
                <h2>Список пользователей</h2>
                <table>
                  <thead>
                    <tr>
                      <th>Имя</th>
                      <th>Роль</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="user in users" :key="user.id">
                      <td>{{ user.username }}</td>
                      <td>{{ user.role }}</td>
                    </tr>
                  </tbody>
                </table>
                <div v-if="users.length === 0" class="empty-state">Пользователи не найдены.</div>
              </div>

              <div class="panel-card create-user-card">
                <h3>Создать нового пользователя</h3>
                <form @submit.prevent="submitUser">
                  <label>
                    Логин
                    <input v-model="username" placeholder="Имя пользователя" />
                  </label>
                  <label>
                    Пароль
                    <input v-model="password" type="password" placeholder="Пароль" />
                  </label>
                  <label>
                    Роль
                    <select v-model="role">
                      <option value="user">User</option>
                      <option value="admin">Admin</option>
                    </select>
                  </label>
                  <button type="submit" class="btn btn-primary">Создать</button>
                </form>
                <p v-if="userMessage" class="success">{{ userMessage }}</p>
                <p v-if="userError" class="error">{{ userError }}</p>
              </div>
            </section>
          </div>

          <!-- Logs Tab -->
          <div v-if="activeTab === 'logs'" class="tab-content">
            <section class="logs-section">
              <div class="panel-card">
                <h2>Журнал активности</h2>
                <div class="empty-state">Функционал журнала в разработке.</div>
              </div>
            </section>
          </div>
        </div>
      </template>
    </main-layout>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import MainLayout from '../../layouts/MainLayout.vue';
import { fetchUsers, createUser } from '../../api/users';

export default {
  name: 'AdminView',
  components: {
    MainLayout
  },
  data() {
    return {
      activeTab: 'users',
      users: [],
      username: '',
      password: '',
      role: 'user',
      userError: '',
      userMessage: ''
    };
  },
  async created() {
    await this.loadUsers();
  },
  methods: {
    async loadUsers() {
      try {
        this.users = await fetchUsers();
      } catch (error) {
        console.error('Error loading users:', error);
        this.userError = 'Не удалось загрузить список пользователей.';
      }
    },
    async submitUser() {
      this.userError = '';
      this.userMessage = '';
      if (!this.username.trim() || !this.password.trim()) {
        this.userError = 'Введите логин и пароль.';
        return;
      }

      try {
        await createUser({ 
          username: this.username.trim(), 
          password: this.password.trim(), 
          role: this.role 
        });
        this.userMessage = 'Пользователь успешно создан.';
        this.username = '';
        this.password = '';
        this.role = 'user';
        await this.loadUsers();
      } catch (error) {
        console.error('Error creating user:', error);
        this.userError = 'Ошибка при создании пользователя.';
      }
    }
  }
};
</script>

<style scoped>
.admin-view {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.tabs {
  display: flex;
  gap: 10px;
  border-bottom: 2px solid #e2e8f0;
  padding-bottom: 0;
}

.tab {
  padding: 12px 20px;
  background: transparent;
  border: none;
  cursor: pointer;
  color: #475569;
  font-weight: 500;
  border-bottom: 3px solid transparent;
  margin-bottom: -2px;
  transition: all 0.3s ease;
}

.tab:hover {
  color: #1e293b;
}

.tab.active {
  color: #2563eb;
  border-bottom-color: #2563eb;
}

.tab-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.user-management {
  display: grid;
  gap: 20px;
}

.panel-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.08);
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 14px;
}

th,
td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e2e8f0;
}

th {
  background: #f8fafc;
  font-weight: 600;
}

label {
  display: block;
  margin-bottom: 14px;
  font-weight: 500;
}

input,
select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  margin-top: 6px;
  font-size: 14px;
}

input:focus,
select:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.btn {
  padding: 10px 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-primary {
  background: #2563eb;
  color: white;
}

.btn-primary:hover {
  background: #1d4ed8;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #94a3b8;
  font-size: 14px;
}

.success {
  margin-top: 12px;
  padding: 10px 12px;
  background: #dcfce7;
  color: #166534;
  border-radius: 8px;
  font-size: 14px;
}

.error {
  margin-top: 12px;
  padding: 10px 12px;
  background: #fee2e2;
  color: #991b1b;
  border-radius: 8px;
  font-size: 14px;
}
</style>
