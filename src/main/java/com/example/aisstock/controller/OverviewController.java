package com.example.aisstock.controller;

import com.example.aisstock.repository.ProductRepository;
import com.example.aisstock.repository.StockItemRepository;
import com.example.aisstock.repository.WarehouseRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class OverviewController {
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockItemRepository stockItemRepository;

    public OverviewController(ProductRepository productRepository,
                              WarehouseRepository warehouseRepository,
                              StockItemRepository stockItemRepository) {
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.stockItemRepository = stockItemRepository;
    }

    @GetMapping("/api/overview")
    public Map<String, Object> overview() {
        long lowStockCount = stockItemRepository.findAll().stream()
                .filter(item -> item.getQuantity() <= 10)
                .count();

        return Map.of(
                "products", productRepository.count(),
                "warehouses", warehouseRepository.count(),
                "stockItems", stockItemRepository.count(),
                "lowStockItems", lowStockCount
        );
    }
}
