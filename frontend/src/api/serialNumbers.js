import api from './http';

export async function fetchSerialNumbers() {
    const response = await api.get('/serial-numbers');
    return response.data;
}

export async function createSerialNumber(serialNumber) {
    const response = await api.post('/serial-numbers', serialNumber);
    return response.data;
}

export async function updateSerialNumber(id, serialNumber) {
    const response = await api.put(`/serial-numbers/${id}`, serialNumber);
    return response.data;
}

export async function deleteSerialNumber(id) {
    const response = await api.delete(`/serial-numbers/${id}`);
    return response.data;
}

export async function fetchSerialNumbersByProduct(productId) {
    const response = await api.get(`/serial-numbers/product/${productId}`);
    return response.data;
}