package com.example.aisstock.service;

import com.example.aisstock.model.ActivityLog;
import com.example.aisstock.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ActivityLogService {
    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    public List<ActivityLog> findAll() {
        return activityLogRepository.findAll();
    }

    public List<ActivityLog> findAdminLogs(String adminUsername, LocalDate from, LocalDate to) {
        String admin = (adminUsername != null && !adminUsername.isBlank()) ? adminUsername.trim() : null;
        LocalDateTime fromDt = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDt = to != null ? to.atTime(LocalTime.MAX) : null;
        return activityLogRepository.findAdminLogs(admin, fromDt, toDt);
    }

    @Transactional
    public ActivityLog log(String username, String action, String details) {
        ActivityLog event = new ActivityLog();
        event.setUsername(username);
        event.setAction(action);
        event.setDetails(details);
        return activityLogRepository.save(event);
    }

    @Transactional
    public ActivityLog logAdmin(String adminUsername, String action, String targetUsername, String details) {
        ActivityLog event = new ActivityLog();
        event.setUsername(adminUsername);
        event.setAction(action);
        event.setTargetUsername(targetUsername);
        event.setDetails(details);
        return activityLogRepository.save(event);
    }
}
