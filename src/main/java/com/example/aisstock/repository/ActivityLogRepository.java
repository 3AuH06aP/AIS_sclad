package com.example.aisstock.repository;

import com.example.aisstock.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    @Query("""
            SELECT l FROM ActivityLog l
            WHERE l.action LIKE 'admin_%'
            AND (:admin IS NULL OR l.username = :admin)
            AND (:from IS NULL OR l.createdAt >= :from)
            AND (:to IS NULL OR l.createdAt <= :to)
            ORDER BY l.createdAt DESC
            """)
    List<ActivityLog> findAdminLogs(
            @Param("admin") String admin,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
