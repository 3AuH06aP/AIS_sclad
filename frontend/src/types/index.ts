export type UserRole = 'admin' | 'storekeeper' | 'user';

/** API returns admin | user (кладовщик = user). */
export type ApiUserRole = 'admin' | 'user';

export interface User {
  id: number;
  username: string;
  role: ApiUserRole;
  createdAt?: string | null;
  enabled: boolean;
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
  batch?: string;
  expirationDate?: string;
}

export interface SerialNumber {
  id: number;
  serialNumber: string;
  product: Product;
  stockItem?: StockItem;
  status?: string;
}

export interface DocumentItem {
  id: number;
  document?: Document;
  product: Product;
  quantity: number;
  batch?: string;
  storageLocation?: string;
  unitPrice?: number;
  notes?: string;
  serialNumbers?: string;
}

export interface Document {
  id: number;
  documentType: 'RECEIPT' | 'SHIPMENT' | 'TRANSFER' | 'WRITE_OFF';
  documentNumber?: string;
  reference?: string;
  warehouseFrom?: Warehouse;
  warehouseTo?: Warehouse;
  createdBy?: string;
  createdAt?: string;
  notes?: string;
  status: 'DRAFT' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED';
  items: DocumentItem[];
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

export interface Task {
  id: number;
  taskType: 'PUTAWAY' | 'PICKING';
  product: Product;
  quantity: number;
  warehouse?: Warehouse;
  storageLocation?: string;
  batch?: string;
  assignedTo?: string;
  notes?: string;
  createdAt?: string;
  completedAt?: string;
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
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
  targetUsername?: string | null;
  createdAt: string;
}
