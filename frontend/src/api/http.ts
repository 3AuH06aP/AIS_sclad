import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json'
  }
});

api.interceptors.request.use((config) => {
  const authJson = localStorage.getItem('ais-stock-auth');
  if (authJson) {
    try {
      const auth = JSON.parse(authJson);
      if (auth?.token) {
        config.headers.Authorization = `Bearer ${auth.token}`;
      }
    } catch {
      // ignore parse errors
    }
  }
  return config;
});

export default api;
