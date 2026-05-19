package com.example.aisstock.repository;

import com.example.aisstock.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByStatus(Task.TaskStatus status);
    List<Task> findByAssignedTo(String assignedTo);
    List<Task> findByTaskType(Task.TaskType taskType);
    List<Task> findByStatusAndAssignedTo(Task.TaskStatus status, String assignedTo);
}