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
}
