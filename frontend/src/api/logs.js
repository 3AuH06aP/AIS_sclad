import api from './http';
export async function fetchLogs() {
    const response = await api.get('/logs');
    return response.data;
}
//# sourceMappingURL=logs.js.map