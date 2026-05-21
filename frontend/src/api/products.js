import api from './http';

export async function fetchProducts() {
    const response = await api.get('/products/summary');
    return response.data;
}

export async function createProduct(product) {
    const response = await api.post('/products', product);
    return response.data;
}

export async function importProductsExcel(file) {
    const formData = new FormData();
    formData.append('file', file);

    const response = await api.post('/products/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    return response.data;
}

export async function fetchProductById(id) {
    const response = await api.get(`/products/${id}`);
    return response.data;
}

export async function searchProductsSummary(query) {
    const q = (query || '').trim();
    if (!q) {
        return [];
    }
    const response = await api.get('/products/search', { params: { query: q } });
    return response.data;
}
