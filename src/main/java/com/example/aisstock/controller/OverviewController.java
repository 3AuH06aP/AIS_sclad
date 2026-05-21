package com.example.aisstock.controller;

import com.example.aisstock.dto.LowStockProductDto;
import com.example.aisstock.dto.OverviewResponse;
import com.example.aisstock.dto.ProductSummary;
import com.example.aisstock.model.StockTransactionType;
import com.example.aisstock.repository.ProductRepository;
import com.example.aisstock.repository.StockItemRepository;
import com.example.aisstock.repository.StockTransactionRepository;
import com.example.aisstock.repository.WarehouseRepository;
import com.example.aisstock.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@RestController
public class OverviewController {

    private static final List<StockTransactionType> INCOMING_TYPES = List.of(
            StockTransactionType.RECEIPT,
            StockTransactionType.PUTAWAY,
            StockTransactionType.PACKING
    );
    private static final List<StockTransactionType> OUTGOING_TYPES = List.of(
            StockTransactionType.ISSUE,
            StockTransactionType.PICKING,
            StockTransactionType.SHIPPING
    );

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockItemRepository stockItemRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final ProductService productService;

    public OverviewController(ProductRepository productRepository,
                              WarehouseRepository warehouseRepository,
                              StockItemRepository stockItemRepository,
                              StockTransactionRepository stockTransactionRepository,
                              ProductService productService) {
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.stockItemRepository = stockItemRepository;
        this.stockTransactionRepository = stockTransactionRepository;
        this.productService = productService;
    }

    @GetMapping("/api/overview")
    public OverviewResponse overview() {
        List<LowStockProductDto> lowStockProducts = productService.findSummary().stream()
                .filter(this::isLowStock)
                .map(this::toLowStockDto)
                .toList();

        LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);
        OffsetDateTime startOfToday = todayUtc.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime startOfTomorrow = todayUtc.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime startOfRollingWeek = todayUtc.minusDays(6).atStartOfDay().atOffset(ZoneOffset.UTC);

        long receiptsToday = stockTransactionRepository.countByTypesAndCreatedAtRange(
                INCOMING_TYPES, startOfToday, startOfTomorrow);
        long issuesToday = stockTransactionRepository.countByTypesAndCreatedAtRange(
                OUTGOING_TYPES, startOfToday, startOfTomorrow);
        long receiptsWeek = stockTransactionRepository.countByTypesAndCreatedAtRange(
                INCOMING_TYPES, startOfRollingWeek, startOfTomorrow);
        long issuesWeek = stockTransactionRepository.countByTypesAndCreatedAtRange(
                OUTGOING_TYPES, startOfRollingWeek, startOfTomorrow);

        Long totalQty = stockItemRepository.sumTotalQuantity();
        long totalStockQuantity = totalQty != null ? totalQty : 0L;

        return new OverviewResponse(
                productRepository.count(),
                warehouseRepository.count(),
                totalStockQuantity,
                lowStockProducts.size(),
                lowStockProducts,
                receiptsToday,
                receiptsWeek,
                issuesToday,
                issuesWeek
        );
    }

    private boolean isLowStock(ProductSummary s) {
        int min = s.minQuantity() != null ? s.minQuantity() : 0;
        int qty = s.quantity() != null ? s.quantity() : 0;
        return qty <= min;
    }

    private LowStockProductDto toLowStockDto(ProductSummary s) {
        return new LowStockProductDto(
                s.id(),
                s.sku(),
                s.name(),
                s.quantity() != null ? s.quantity() : 0,
                s.minQuantity() != null ? s.minQuantity() : 0
        );
    }
}
