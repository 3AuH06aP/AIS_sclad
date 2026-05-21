import api from './http';

export interface LowStockProduct {
  productId: number;
  sku: string;
  name: string;
  quantity: number;
  minQuantity: number;
}

export interface Overview {
  productLinesCount: number;
  warehousesCount: number;
  totalStockQuantity: number;
  lowStockCount: number;
  lowStockProducts: LowStockProduct[];
  receiptsToday: number;
  receiptsWeek: number;
  issuesToday: number;
  issuesWeek: number;
}

export async function fetchOverview(): Promise<Overview> {
  const response = await api.get<Overview>('/overview');
  return response.data;
}
