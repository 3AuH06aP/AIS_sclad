import api from './http';
export async function fetchProducts() {
    const response = await api.get('/products/summary');
    return response.data;
}
export async function createProduct(product) {
    const response = await api.post('/products', product);
    return response.data;
}
//# sourceMappingURL=products.js.map