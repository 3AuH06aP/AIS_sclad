package com.example.aisstock.controller;

import com.example.aisstock.model.Product;
import com.example.aisstock.model.StockItem;
import com.example.aisstock.model.Warehouse;
import com.example.aisstock.repository.ProductRepository;
import com.example.aisstock.repository.WarehouseRepository;
import com.example.aisstock.service.ActivityLogService;
import com.example.aisstock.service.StockService;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/stock")
@Validated
public class StockController {
    private final StockService stockService;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final ActivityLogService activityLogService;

    public StockController(StockService stockService,
                           ProductRepository productRepository,
                           WarehouseRepository warehouseRepository,
                           ActivityLogService activityLogService) {
        this.stockService = stockService;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    public List<StockItem> list() {
        return stockService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockItem> get(@PathVariable Long id) {
        return stockService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StockItem> create(@RequestHeader(value = "X-User-Name", defaultValue = "anonymous") String currentUser,
                                            @RequestBody StockItem stockItem) {
        if (stockItem.getProduct() == null || stockItem.getWarehouse() == null) {
            return ResponseEntity.badRequest().build();
        }

        Product product = productRepository.findById(stockItem.getProduct().getId()).orElse(null);
        Warehouse warehouse = warehouseRepository.findById(stockItem.getWarehouse().getId()).orElse(null);
        if (product == null || warehouse == null) {
            return ResponseEntity.badRequest().build();
        }

        stockItem.setProduct(product);
        stockItem.setWarehouse(warehouse);
        StockItem saved = stockService.save(stockItem);
        activityLogService.log(currentUser, "create_stock_item", "Product " + product.getSku() + " in warehouse " + warehouse.getName() + " quantity " + saved.getQuantity());
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{id}/adjust")
    public ResponseEntity<StockItem> adjust(@RequestHeader(value = "X-User-Name", defaultValue = "anonymous") String currentUser,
                                            @PathVariable Long id,
                                            @RequestParam @Min(-100000) int delta) {
        return stockService.findById(id)
                .map(item -> {
                    StockItem updated = stockService.adjustStock(item, delta);
                    activityLogService.log(currentUser, "adjust_stock", "Adjusted stock item " + updated.getId() + " by " + delta + " (product " + updated.getProduct().getSku() + ", warehouse " + updated.getWarehouse().getName() + ")");
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stockService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
