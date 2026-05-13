package com.example.aisstock.repository;

import com.example.aisstock.model.StockItem;
import com.example.aisstock.model.Product;
import com.example.aisstock.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface StockItemRepository extends JpaRepository<StockItem, Long> {
    Optional<StockItem> findByProductAndWarehouse(Product product, Warehouse warehouse);
    Optional<StockItem> findByProductAndWarehouseAndStorageLocation(Product product, Warehouse warehouse, String storageLocation);
    List<StockItem> findByWarehouse(Warehouse warehouse);
    List<StockItem> findByProduct(Product product);
}
