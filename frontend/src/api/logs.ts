import api from './http';
import type { ActivityLog } from '../types';

export interface AdminLogFilters {
  admin?: string;
  from?: string;
  to?: string;
}

export async function fetchLogs(): Promise<ActivityLog[]> {
  const response = await api.get<ActivityLog[]>('/logs');
  return response.data;
}

export async function fetchAdminLogs(filters: AdminLogFilters = {}): Promise<ActivityLog[]> {
  const params: Record<string, string> = {};
  if (filters.admin?.trim()) {
    params.admin = filters.admin.trim();
  }
  if (filters.from) {
    params.from = filters.from;
  }
  if (filters.to) {
    params.to = filters.to;
  }
  const response = await api.get<ActivityLog[]>('/logs/admin', { params });
  return response.data;
}

export const ADMIN_ACTION_LABELS: Record<string, string> = {
  admin_create_user: 'Создание пользователя',
  admin_reset_password: 'Сброс пароля',
  admin_delete_user: 'Удаление пользователя',
  admin_block_user: 'Блокировка',
  admin_unblock_user: 'Разблокировка'
};
