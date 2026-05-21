package com.example.aisstock.dto;

import java.math.BigDecimal;

public record ProductSummary(
        Long id,
        String sku,
        String barcode,
        String name,
        String description,
        String category,
        String unit,
        String inventoryClass,
        String trackingMethod,
        Integer minQuantity,
        Integer quantity,
        BigDecimal purchasePrice,
        BigDecimal salePrice
) {
}
