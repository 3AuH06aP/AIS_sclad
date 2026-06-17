package com.example.aisstock.repository;

import com.example.aisstock.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    @Query(value = """
    SELECT * FROM activity_logs l
    WHERE (:admin IS NULL OR l.username = CAST(:admin AS text))
    AND (:action IS NULL OR l.action ILIKE CONCAT('%', CAST(:action AS text), '%'))
    AND (CAST(:from AS timestamp) IS NULL OR l.created_at >= CAST(:from AS timestamp))
    AND (CAST(:to AS timestamp) IS NULL OR l.created_at <= CAST(:to AS timestamp))
    ORDER BY l.created_at DESC
    """, nativeQuery = true)
    List<ActivityLog> findLogs(
            @Param("admin") String admin,
            @Param("action") String action,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
