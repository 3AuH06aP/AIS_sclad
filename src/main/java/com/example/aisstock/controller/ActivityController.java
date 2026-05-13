package com.example.aisstock.controller;

import com.example.aisstock.model.ActivityLog;
import com.example.aisstock.service.ActivityLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class ActivityController {
    private final ActivityLogService activityLogService;

    public ActivityController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping
    public List<ActivityLog> list() {
        return activityLogService.findAll();
    }
}
