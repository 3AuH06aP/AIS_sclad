import api from './http';

export interface WarehouseRef {
  id?: number;
  name?: string;
}

export interface TaskProduct {
  id: number;
  name: string;
  unit?: string;
}

export interface TaskItem {
  id: number;
  status: string;
  taskType: string;
  quantity: number;
  product: TaskProduct;
  warehouse?: WarehouseRef;
}

export async function fetchTasks(): Promise<TaskItem[]> {
  const response = await api.get<TaskItem[]>('/tasks');
  return response.data;
}

export async function completeTask(id: number): Promise<TaskItem> {
  const response = await api.put<TaskItem>(`/tasks/${id}/complete`);
  return response.data;
}

export async function updateTask(id: number, task: unknown): Promise<TaskItem> {
  const response = await api.put<TaskItem>(`/tasks/${id}`, task);
  return response.data;
}
