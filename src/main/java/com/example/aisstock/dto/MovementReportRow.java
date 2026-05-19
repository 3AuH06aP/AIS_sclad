package com.example.aisstock.dto;

import java.time.OffsetDateTime;

public record MovementReportRow(
        Long id,
        OffsetDateTime transactionDate,
        String type,
        String transactionType,
        String productSku,
        String productName,
        String warehouseName,
        Integer quantity,
        String createdBy,
        String reference,
        String notes
) {
}
