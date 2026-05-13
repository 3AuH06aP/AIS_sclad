import api from './http';
import type { StockItemCreateRequest, StockItem, StockTransactionRequest, StockTransaction } from '../types';

export async function fetchStockItems(): Promise<StockItem[]> {
  const response = await api.get<StockItem[]>('/stock');
  return response.data;
}

export async function adjustStock(id: number, delta: number): Promise<StockItem> {
  const response = await api.post<StockItem>(`/stock/${id}/adjust`, null, { params: { delta } });
  return response.data;
}

export async function createStockItem(request: StockItemCreateRequest): Promise<StockItem> {
  const response = await api.post<StockItem>('/stock', request);
  return response.data;
}

export async function createStockTransaction(request: StockTransactionRequest): Promise<StockTransaction> {
  const response = await api.post<StockTransaction>('/stock/transactions', request);
  return response.data;
}

export async function fetchStockTransactions(): Promise<StockTransaction[]> {
  const response = await api.get<StockTransaction[]>('/stock/transactions');
  return response.data;
}
