import api from './http';

export interface AuthResponse {
  id: number;
  username: string;
  role: 'admin' | 'user';
}

export async function login(username: string, password: string): Promise<AuthResponse> {
  const response = await api.post<AuthResponse>('/auth/login', { username, password });
  return response.data;
}
