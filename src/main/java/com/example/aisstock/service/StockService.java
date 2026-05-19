package com.example.aisstock.service;

import com.example.aisstock.model.Product;
import com.example.aisstock.model.StockItem;
import com.example.aisstock.model.Warehouse;
import com.example.aisstock.repository.StockItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class StockService {
    private final StockItemRepository stockItemRepository;

    public StockService(StockItemRepository stockItemRepository) {
        this.stockItemRepository = stockItemRepository;
    }

    public List<StockItem> findAll() {
        return stockItemRepository.findAll();
    }

    public List<StockItem> findByWarehouse(Warehouse warehouse) {
        return stockItemRepository.findByWarehouse(warehouse);
    }

    public Optional<StockItem> findByProductAndWarehouse(Product product, Warehouse warehouse) {
        return stockItemRepository.findByProductAndWarehouse(product, warehouse);
    }

    public Optional<StockItem> findByProductAndWarehouseAndLocation(Product product, Warehouse warehouse, String storageLocation) {
        if (storageLocation == null || storageLocation.isBlank()) {
            return stockItemRepository.findByProductAndWarehouse(product, warehouse);
        }
        return stockItemRepository.findByProductAndWarehouseAndStorageLocation(product, warehouse, storageLocation);
    }

    public List<StockItem> findByProduct(Product product) {
        return stockItemRepository.findByProduct(product);
    }

    public Optional<StockItem> findById(Long id) {
        return stockItemRepository.findById(id);
    }

    @Transactional
    public StockItem save(StockItem stockItem) {
        return stockItemRepository.save(stockItem);
    }

    @Transactional
    public StockItem adjustStock(StockItem stockItem, int delta) {
        stockItem.setQuantity(Math.max(0, stockItem.getQuantity() + delta));
        return stockItemRepository.save(stockItem);
    }

    @Transactional
    public void deleteById(Long id) {
        stockItemRepository.deleteById(id);
    }

    @Transactional
    public StockItem addStock(Long productId, Long warehouseId, Integer quantity, String storageLocation, String batch, java.time.LocalDate expirationDate) {
        storageLocation = normalize(storageLocation);
        batch = normalize(batch);

        // Try exact match first
        Optional<StockItem> existing = stockItemRepository.findStockItemRobust(productId, warehouseId, storageLocation, batch);
        
        // If not found, try by product/warehouse if that's the constraint
        if (existing.isEmpty()) {
             existing = stockItemRepository.findByProductIdAndWarehouseId(productId, warehouseId);
        }

        if (existing.isPresent()) {
            StockItem stockItem = existing.get();
            stockItem.setQuantity(stockItem.getQuantity() + quantity);
            // Optionally update location/batch if they were null in DB but present in request
            if (stockItem.getStorageLocation() == null) stockItem.setStorageLocation(storageLocation);
            if (stockItem.getBatch() == null) stockItem.setBatch(batch);
            return stockItemRepository.save(stockItem);
        } else {
            StockItem stockItem = new StockItem();
            stockItem.setProduct(new Product(productId));
            stockItem.setWarehouse(new Warehouse(warehouseId));
            stockItem.setQuantity(quantity);
            stockItem.setStorageLocation(storageLocation);
            stockItem.setBatch(batch);
            stockItem.setExpirationDate(expirationDate);
            return stockItemRepository.save(stockItem);
        }
    }

    @Transactional
    public StockItem removeStock(Long productId, Long warehouseId, Integer quantity, String storageLocation, String batch) {
        storageLocation = normalize(storageLocation);
        batch = normalize(batch);

        Optional<StockItem> existing = stockItemRepository.findStockItemRobust(productId, warehouseId, storageLocation, batch);
        if (existing.isEmpty()) {
             existing = stockItemRepository.findByProductIdAndWarehouseId(productId, warehouseId);
        }

        if (existing.isPresent()) {
            StockItem stockItem = existing.get();
            if (stockItem.getQuantity() < quantity) {
                throw new RuntimeException("Недостаточно товара на складе (SKU: " + stockItem.getProduct().getSku() + "). В наличии: " + stockItem.getQuantity());
            }
            stockItem.setQuantity(stockItem.getQuantity() - quantity);
            return stockItemRepository.save(stockItem);
        }
        throw new RuntimeException("Товар не найден на складе для проведения операции");
    }

    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
