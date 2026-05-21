package com.example.aisstock.dto;

import java.util.List;

public record OverviewResponse(
        long productLinesCount,
        long warehousesCount,
        long totalStockQuantity,
        long lowStockCount,
        List<LowStockProductDto> lowStockProducts,
        long receiptsToday,
        long receiptsWeek,
        long issuesToday,
        long issuesWeek
) {
}
