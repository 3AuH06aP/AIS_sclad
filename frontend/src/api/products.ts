import api from './http';
import type { Product } from '../types';

export async function fetchProducts(): Promise<Product[]> {
  const response = await api.get<Product[]>('/products/summary');
  return response.data;
}

export async function createProduct(product: Product): Promise<Product> {
  const response = await api.post<Product>('/products', product);
  return response.data;
}
