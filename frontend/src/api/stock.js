import api from './http';
export async function fetchStockItems() {
    const response = await api.get('/stock');
    return response.data;
}
export async function adjustStock(id, delta) {
    const response = await api.post(`/stock/${id}/adjust`, null, { params: { delta } });
    return response.data;
}
export async function createStockItem(request) {
    const response = await api.post('/stock', request);
    return response.data;
}
export async function createStockTransaction(request) {
    const response = await api.post('/stock/transactions', request);
    return response.data;
}
export async function fetchStockTransactions() {
    const response = await api.get('/stock/transactions');
    return response.data;
}
//# sourceMappingURL=stock.js.map