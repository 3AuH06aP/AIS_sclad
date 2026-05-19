package com.example.aisstock.service;

import com.example.aisstock.model.Task;
import com.example.aisstock.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public List<Task> getTasksByStatus(Task.TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    public List<Task> getTasksByAssignedTo(String assignedTo) {
        return taskRepository.findByAssignedTo(assignedTo);
    }

    public List<Task> getPendingTasksForUser(String assignedTo) {
        return taskRepository.findByStatusAndAssignedTo(Task.TaskStatus.PENDING, assignedTo);
    }

    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }

    public Task createPutawayTask(Task task) {
        task.setTaskType(Task.TaskType.PUTAWAY);
        task.setStatus(Task.TaskStatus.PENDING);
        return taskRepository.save(task);
    }

    public Task createPickingTask(Task task) {
        task.setTaskType(Task.TaskType.PICKING);
        task.setStatus(Task.TaskStatus.PENDING);
        return taskRepository.save(task);
    }

    public Task completeTask(Long taskId) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (optionalTask.isPresent()) {
            Task task = optionalTask.get();
            task.setStatus(Task.TaskStatus.COMPLETED);
            task.setCompletedAt(java.time.OffsetDateTime.now());
            return taskRepository.save(task);
        }
        throw new RuntimeException("Task not found");
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
}