package com.example.aisstock.repository;

import com.example.aisstock.model.StockItem;
import com.example.aisstock.model.Product;
import com.example.aisstock.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface StockItemRepository extends JpaRepository<StockItem, Long> {
    Optional<StockItem> findByProductAndWarehouse(Product product, Warehouse warehouse);
    Optional<StockItem> findByProductIdAndWarehouseId(Long productId, Long warehouseId);
    Optional<StockItem> findByProductAndWarehouseAndStorageLocation(Product product, Warehouse warehouse, String storageLocation);
    List<StockItem> findByWarehouse(Warehouse warehouse);
    List<StockItem> findByProduct(Product product);

    @Query("SELECT s FROM StockItem s WHERE s.product.id = :productId AND s.warehouse.id = :warehouseId " +
           "AND ((:storageLocation IS NULL AND s.storageLocation IS NULL) OR (s.storageLocation = :storageLocation)) " +
           "AND ((:batch IS NULL AND s.batch IS NULL) OR (s.batch = :batch))")
    Optional<StockItem> findStockItemRobust(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId,
            @Param("storageLocation") String storageLocation,
            @Param("batch") String batch);

    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM StockItem s")
    Long sumTotalQuantity();
}