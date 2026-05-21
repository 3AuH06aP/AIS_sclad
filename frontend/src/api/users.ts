import api from './http';
import type { User } from '../types';

export interface CreateUserRequest {
  username: string;
  password: string;
  passwordConfirm?: string;
}

export interface ResetPasswordResult {
  username: string;
  newPassword: string;
}

function adminHeaders() {
  const authJson = localStorage.getItem('ais-stock-auth');
  const headers: Record<string, string> = {};
  if (authJson) {
    try {
      const auth = JSON.parse(authJson);
      if (auth?.username) {
        headers['X-User-Name'] = auth.username;
      }
    } catch {
      // ignore
    }
  }
  return headers;
}

export async function fetchUsers(): Promise<User[]> {
  const response = await api.get<User[]>('/users');
  return response.data;
}

export async function createUser(request: CreateUserRequest): Promise<User> {
  const response = await api.post<User>('/users', request, { headers: adminHeaders() });
  return response.data;
}

export async function resetUserPassword(userId: number): Promise<ResetPasswordResult> {
  const response = await api.post<ResetPasswordResult>(`/users/${userId}/reset-password`, null, {
    headers: adminHeaders()
  });
  return response.data;
}

export async function setUserEnabled(userId: number, enabled: boolean): Promise<User> {
  const response = await api.put<User>(`/users/${userId}/enabled`, { enabled }, { headers: adminHeaders() });
  return response.data;
}

export async function deleteUser(userId: number): Promise<void> {
  await api.delete(`/users/${userId}`, { headers: adminHeaders() });
}

export function generatePassword(length = 10): string {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789';
  let result = '';
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}
