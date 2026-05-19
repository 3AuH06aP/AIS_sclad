package com.example.aisstock.controller;

import com.example.aisstock.model.Task;
import com.example.aisstock.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public List<Task> getTasksByStatus(@PathVariable Task.TaskStatus status) {
        return taskService.getTasksByStatus(status);
    }

    @GetMapping("/assigned/{assignedTo}")
    public List<Task> getTasksByAssignedTo(@PathVariable String assignedTo) {
        return taskService.getTasksByAssignedTo(assignedTo);
    }

    @GetMapping("/pending/{assignedTo}")
    public List<Task> getPendingTasksForUser(@PathVariable String assignedTo) {
        return taskService.getPendingTasksForUser(assignedTo);
    }

    @PostMapping
    public Task createTask(@RequestBody Task task) {
        return taskService.saveTask(task);
    }

    @PostMapping("/putaway")
    public Task createPutawayTask(@RequestBody Task task) {
        return taskService.createPutawayTask(task);
    }

    @PostMapping("/picking")
    public Task createPickingTask(@RequestBody Task task) {
        return taskService.createPickingTask(task);
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Task> completeTask(@PathVariable Long id) {
        try {
            Task task = taskService.completeTask(id);
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        if (!taskService.getTaskById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        task.setId(id);
        return ResponseEntity.ok(taskService.saveTask(task));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        if (!taskService.getTaskById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}