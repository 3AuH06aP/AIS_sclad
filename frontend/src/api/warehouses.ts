import api from './http';
import type { Warehouse } from '../types';

export async function fetchWarehouses(): Promise<Warehouse[]> {
  const response = await api.get<Warehouse[]>('/warehouses');
  return response.data;
}
