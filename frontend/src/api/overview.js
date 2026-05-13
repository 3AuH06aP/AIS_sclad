import api from './http';
export async function fetchOverview() {
    const response = await api.get('/overview');
    return response.data;
}
//# sourceMappingURL=overview.js.map