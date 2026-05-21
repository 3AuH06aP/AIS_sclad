import api from './http';

function adminHeaders() {
    const authJson = localStorage.getItem('ais-stock-auth');
    const headers = {};
    if (authJson) {
        try {
            const auth = JSON.parse(authJson);
            if (auth?.username) {
                headers['X-User-Name'] = auth.username;
            }
        }
        catch {
            // ignore
        }
    }
    return headers;
}

export async function fetchUsers() {
    const response = await api.get('/users');
    return response.data;
}

export async function createUser(request) {
    const response = await api.post('/users', request, { headers: adminHeaders() });
    return response.data;
}

export async function resetUserPassword(userId) {
    const response = await api.post(`/users/${userId}/reset-password`, null, { headers: adminHeaders() });
    return response.data;
}

export async function setUserEnabled(userId, enabled) {
    const response = await api.put(`/users/${userId}/enabled`, { enabled }, { headers: adminHeaders() });
    return response.data;
}

export async function deleteUser(userId) {
    await api.delete(`/users/${userId}`, { headers: adminHeaders() });
}

export function generatePassword(length = 10) {
    const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789';
    let result = '';
    for (let i = 0; i < length; i++) {
        result += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return result;
}
