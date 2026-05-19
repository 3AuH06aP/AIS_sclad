import api from './http';

export async function fetchDocuments() {
    const response = await api.get('/documents');
    return response.data;
}

export async function fetchDocumentById(id) {
    const response = await api.get(`/documents/${id}`);
    return response.data;
}

export async function createReceiptDocument(document) {
    const response = await api.post('/documents/receipt', document);
    return response.data;
}

export async function createShipmentDocument(document) {
    const response = await api.post('/documents/shipment', document);
    return response.data;
}

export async function createTransferDocument(document) {
    const response = await api.post('/documents/transfer', document);
    return response.data;
}

export async function createWriteOffDocument(document) {
    const response = await api.post('/documents/write-off', document);
    return response.data;
}

export async function confirmDocument(id) {
    const response = await api.put(`/documents/${id}/confirm`);
    return response.data;
}

export async function completeDocument(id) {
    const response = await api.put(`/documents/${id}/complete`);
    return response.data;
}

export async function updateDocument(id, document) {
    const response = await api.put(`/documents/${id}`, document);
    return response.data;
}

export async function deleteDocument(id) {
    const response = await api.delete(`/documents/${id}`);
    return response.data;
}

export async function fetchDocumentsByType(type) {
    const response = await api.get(`/documents/type/${type}`);
    return response.data;
}
