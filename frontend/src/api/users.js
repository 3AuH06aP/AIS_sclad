import api from './http';
export async function fetchUsers() {
    const response = await api.get('/users');
    return response.data;
}
export async function createUser(request) {
    const response = await api.post('/users', request);
    return response.data;
}
//# sourceMappingURL=users.js.map