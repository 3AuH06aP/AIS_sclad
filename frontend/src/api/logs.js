import api from './http';

export const ADMIN_ACTION_LABELS = {
    admin_create_user: 'Создание пользователя',
    admin_reset_password: 'Сброс пароля',
    admin_delete_user: 'Удаление пользователя',
    admin_block_user: 'Блокировка',
    admin_unblock_user: 'Разблокировка'
};

export async function fetchLogs() {
    const response = await api.get('/logs');
    return response.data;
}

export async function fetchAdminLogs(filters = {}) {
    const params = {};
    if (filters.admin?.trim()) {
        params.admin = filters.admin.trim();
    }
    if (filters.from) {
        params.from = filters.from;
    }
    if (filters.to) {
        params.to = filters.to;
    }
    const response = await api.get('/logs/admin', { params });
    return response.data;
}
