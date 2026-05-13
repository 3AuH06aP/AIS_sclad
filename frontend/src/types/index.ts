export type UserRole = 'admin' | 'user';

export interface User {
  id: number;
  username: string;
  role: UserRole;
}

export interface Product {
  id: number;
  sku: string;
  name: string;
  description?: string;
  category: string;
  unit: string;
  inventoryClass?: string;
  trackingMethod?: string;
  quantity: number;
  minQuantity: number;
  purchasePrice?: number;
  salePrice?: number;
}

export interface Warehouse {
  id: number;
  name: string;
  location?: string;
}

export interface StockItem {
  id: number;
  product: Product;
  warehouse: Warehouse;
  quantity: number;
  storageLocation?: string;
}

export interface StockItemCreateRequest {
  product: { id: number };
  warehouse: { id: number };
  quantity: number;
}

export interface StockTransactionRequest {
  productId: number;
  warehouseId: number;
  transactionType: 'RECEIPT' | 'ISSUE' | 'PUTAWAY' | 'PICKING' | 'PACKING' | 'SHIPPING';
  quantity: number;
  storageLocation?: string;
  reference?: string;
  notes?: string;
}

export interface StockTransaction {
  id: number;
  transactionType: string;
  stockItem: StockItem;
  quantity: number;
  location?: string;
  reference?: string;
  notes?: string;
  createdBy?: string;
  createdAt: string;
}

export interface ActivityLog {
  id: number;
  username: string;
  action: string;
  details?: string;
  createdAt: string;
}
