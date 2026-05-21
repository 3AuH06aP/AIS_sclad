import api from './http';

export interface AuthResponse {
  id: number;
  username: string;
  role: 'admin' | 'storekeeper' | 'user';
  token?: string;
  fullName?: string | null;
  lastLoginAt?: string | null;
}

export async function login(username: string, password: string): Promise<AuthResponse> {
  const response = await api.post<AuthResponse>('/auth/login', { username, password });
  return response.data;
}
