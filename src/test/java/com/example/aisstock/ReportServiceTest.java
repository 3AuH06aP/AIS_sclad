package com.example.aisstock;

import com.example.aisstock.dto.MovementReportRow;
import com.example.aisstock.dto.ProductSummary;
import com.example.aisstock.model.*;
import com.example.aisstock.repository.ProductRepository;
import com.example.aisstock.repository.StockItemRepository;
import com.example.aisstock.repository.StockTransactionRepository;
import com.example.aisstock.repository.WarehouseRepository;
import com.example.aisstock.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReportServiceTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private StockTransactionRepository transactionRepository;

    @Autowired
    private StockItemRepository stockItemRepository;

    private Product product;
    private Warehouse warehouse;
    private StockItem stockItem;

    @BeforeEach
    void setUp() {
        warehouse = new Warehouse();
        warehouse.setName("Report WH");
        warehouse = warehouseRepository.save(warehouse);

        product = new Product();
        product.setSku("RPT-001");
        product.setName("Report Product");
        product.setUnit("шт");
        product.setMinQuantity(5);
        product.setPurchasePrice(BigDecimal.ONE);
        product = productRepository.save(product);

        stockItem = new StockItem();
        stockItem.setProduct(product);
        stockItem.setWarehouse(warehouse);
        stockItem.setQuantity(20);
        stockItem = stockItemRepository.save(stockItem);
    }

    @Test
    void stockReportAggregatesQuantityByProduct() {
        List<ProductSummary> summary = reportService.getStockByProduct();
        ProductSummary row = summary.stream()
                .filter(item -> "RPT-001".equals(item.sku()))
                .findFirst()
                .orElseThrow();

        assertEquals(20, row.quantity());
    }

    @Test
    void movementsReportFiltersByDateRange() {
        saveTransaction(OffsetDateTime.parse("2026-01-10T10:00:00Z"), StockTransactionType.RECEIPT);
        saveTransaction(OffsetDateTime.parse("2026-02-15T10:00:00Z"), StockTransactionType.ISSUE);

        List<MovementReportRow> january = reportService.getMovements(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31));
        assertEquals(1, january.size());
        assertEquals("IN", january.get(0).type());

        List<MovementReportRow> february = reportService.getMovements(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28));
        assertEquals(1, february.size());
        assertEquals("OUT", february.get(0).type());
    }

    private void saveTransaction(OffsetDateTime createdAt, StockTransactionType type) {
        StockTransaction tx = new StockTransaction();
        tx.setStockItem(stockItem);
        tx.setTransactionType(type);
        tx.setQuantity(3);
        tx.setCreatedBy("tester");
        tx.setCreatedAt(createdAt);
        transactionRepository.save(tx);
    }
}
