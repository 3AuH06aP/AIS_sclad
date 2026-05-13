import api from './http';
import type { ActivityLog } from '../types';

export async function fetchLogs(): Promise<ActivityLog[]> {
  const response = await api.get<ActivityLog[]>('/logs');
  return response.data;
}
