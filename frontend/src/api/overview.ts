import api from './http';

export interface Overview {
  products: number;
  warehouses: number;
  stockItems: number;
  lowStockItems: number;
}

export async function fetchOverview(): Promise<Overview> {
  const response = await api.get<Overview>('/overview');
  return response.data;
}
