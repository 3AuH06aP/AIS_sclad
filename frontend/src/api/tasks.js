import api from './http';

export async function fetchTasks() {
    const response = await api.get('/tasks');
    return response.data;
}

export async function fetchPendingTasksForUser(assignedTo) {
    const response = await api.get(`/tasks/pending/${assignedTo}`);
    return response.data;
}

export async function createPutawayTask(task) {
    const response = await api.post('/tasks/putaway', task);
    return response.data;
}

export async function createPickingTask(task) {
    const response = await api.post('/tasks/picking', task);
    return response.data;
}

export async function completeTask(id) {
    const response = await api.put(`/tasks/${id}/complete`);
    return response.data;
}

export async function updateTask(id, task) {
    const response = await api.put(`/tasks/${id}`, task);
    return response.data;
}

export async function deleteTask(id) {
    const response = await api.delete(`/tasks/${id}`);
    return response.data;
}