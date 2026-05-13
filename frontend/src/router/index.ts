import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import LoginView from '../features/auth/LoginView.vue';
import DashboardView from '../features/dashboard/DashboardView.vue';
import ProductListView from '../features/products/ProductListView.vue';
import TaskView from '../features/tasks/TaskView.vue';
import AdminDashboardView from '../features/admin/AdminDashboardView.vue';
import AdminUsersView from '../features/admin/AdminUsersView.vue';
import AdminActivityView from '../features/admin/AdminActivityView.vue';

const routes = [
  { path: '/login', component: LoginView },
  {
    path: '/',
    component: DashboardView,
    meta: { auth: true }
  },
  {
    path: '/products',
    component: ProductListView,
    meta: { auth: true }
  },
  {
    path: '/tasks',
    component: TaskView,
    meta: { auth: true }
  },
  {
    path: '/admin',
    component: AdminDashboardView,
    meta: { auth: true, admin: true }
  },
  {
    path: '/admin/users',
    component: AdminUsersView,
    meta: { auth: true, admin: true }
  },
  {
    path: '/admin/logs',
    component: AdminActivityView,
    meta: { auth: true, admin: true }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, from, next) => {
  const auth = useAuthStore();
  const requiresAuth = to.meta.auth === true;
  const requiresAdmin = to.meta.admin === true;
  if (requiresAuth && !auth.isAuthenticated) {
    next('/login');
  } else if (requiresAdmin && auth.role !== 'admin') {
    next('/');
  } else {
    next();
  }
});

export default router;
