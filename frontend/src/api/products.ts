import api from './http';
import type { Product } from '../types';

export async function fetchProducts(): Promise<Product[]> {
  const response = await api.get<Product[]>('/products/summary');
  return response.data;
}

export async function fetchProductById(id: number): Promise<Product> {
  const response = await api.get<Product>(`/products/${id}`);
  return response.data;
}

export async function searchProductsSummary(query: string): Promise<Product[]> {
  const q = query.trim();
  if (!q) {
    return [];
  }
  const response = await api.get<Product[]>('/products/search', { params: { query: q } });
  return response.data;
}

export async function createProduct(product: Product): Promise<Product> {
  const response = await api.post<Product>('/products', product);
  return response.data;
}

export async function importProductsExcel(file: File): Promise<any> {
  const formData = new FormData();
  formData.append('file', file);

  const response = await api.post('/products/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
  return response.data;
}
