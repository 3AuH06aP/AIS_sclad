import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json'
  }
});

api.interceptors.request.use((config) => {
  const headers = (config.headers ?? {}) as Record<string, string>;
  const authJson = localStorage.getItem('ais-stock-auth');
  if (authJson) {
    try {
      const auth = JSON.parse(authJson);
      if (auth?.username) {
        headers['X-User-Name'] = auth.username;
      }
    } catch {
      // ignore parse errors
    }
  }
  config.headers = headers as any;
  return config;
});

export default api;
