package com.example.aisstock.controller;

import com.example.aisstock.dto.StockTransactionRequest;
import com.example.aisstock.model.Product;
import com.example.aisstock.model.StockItem;
import com.example.aisstock.model.StockTransaction;
import com.example.aisstock.model.StockTransactionType;
import com.example.aisstock.model.Warehouse;
import com.example.aisstock.service.ActivityLogService;
import com.example.aisstock.service.ProductService;
import com.example.aisstock.service.StockService;
import com.example.aisstock.service.StockTransactionService;
import com.example.aisstock.service.WarehouseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock/transactions")
@Validated
public class StockTransactionController {
    private final StockTransactionService transactionService;
    private final StockService stockService;
    private final ProductService productService;
    private final WarehouseService warehouseService;
    private final ActivityLogService activityLogService;

    public StockTransactionController(StockTransactionService transactionService,
                                      StockService stockService,
                                      ProductService productService,
                                      WarehouseService warehouseService,
                                      ActivityLogService activityLogService) {
        this.transactionService = transactionService;
        this.stockService = stockService;
        this.productService = productService;
        this.warehouseService = warehouseService;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    public List<StockTransaction> list() {
        return transactionService.findAll();
    }

    @PostMapping
    public ResponseEntity<StockTransaction> create(@RequestHeader(value = "X-User-Name", defaultValue = "anonymous") String currentUser,
                                                   @Valid @RequestBody StockTransactionRequest request) {
        Product product = productService.findById(request.getProductId()).orElse(null);
        Warehouse warehouse = warehouseService.findById(request.getWarehouseId()).orElse(null);
        if (product == null || warehouse == null) {
            return ResponseEntity.badRequest().build();
        }

        String storageLocation = request.getStorageLocation();
        StockItem stockItem = stockService.findByProductAndWarehouseAndLocation(product, warehouse, storageLocation)
                .orElseGet(() -> {
                    StockItem item = new StockItem();
                    item.setProduct(product);
                    item.setWarehouse(warehouse);
                    item.setQuantity(0);
                    item.setStorageLocation(storageLocation);
                    return item;
                });

        int delta = request.getTransactionType().isOutgoing() ? -request.getQuantity() : request.getQuantity();
        if (stockItem.getQuantity() + delta < 0) {
            return ResponseEntity.badRequest().build();
        }

        stockItem.setQuantity(stockItem.getQuantity() + delta);
        if (storageLocation != null && !storageLocation.isBlank()) {
            stockItem.setStorageLocation(storageLocation);
        }
        stockItem = stockService.save(stockItem);

        StockTransaction transaction = new StockTransaction();
        transaction.setTransactionType(request.getTransactionType());
        transaction.setStockItem(stockItem);
        transaction.setQuantity(request.getQuantity());
        transaction.setLocation(storageLocation);
        transaction.setReference(request.getReference());
        transaction.setNotes(request.getNotes());
        transaction.setCreatedBy(currentUser);
        transaction = transactionService.save(transaction);

        activityLogService.log(currentUser, "stock_transaction", request.getTransactionType().name() + " product " + product.getSku() + " qty " + request.getQuantity() + " warehouse " + warehouse.getName());
        return ResponseEntity.ok(transaction);
    }
}
