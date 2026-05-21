package com.example.aisstock.dto;

public record LowStockProductDto(
        long productId,
        String sku,
        String name,
        int quantity,
        int minQuantity
) {
}
