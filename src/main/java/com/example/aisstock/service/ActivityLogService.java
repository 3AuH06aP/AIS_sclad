package com.example.aisstock.service;

import com.example.aisstock.model.ActivityLog;
import com.example.aisstock.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public ActivityLog log(String username, String action, String details) {
        ActivityLog event = new ActivityLog();
        event.setUsername(username);
        event.setAction(action);
        event.setDetails(details);
        return activityLogRepository.save(event);
    }
}
