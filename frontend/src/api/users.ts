import api from './http';
import type { User } from '../types';

export interface CreateUserRequest {
  username: string;
  password: string;
  role: 'admin' | 'user';
}

export async function fetchUsers(): Promise<User[]> {
  const response = await api.get<User[]>('/users');
  return response.data;
}

export async function createUser(request: CreateUserRequest): Promise<User> {
  const response = await api.post<User>('/users', request);
  return response.data;
}
