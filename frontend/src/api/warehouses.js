import api from './http';
export async function fetchWarehouses() {
    const response = await api.get('/warehouses');
    return response.data;
}
//# sourceMappingURL=warehouses.js.map