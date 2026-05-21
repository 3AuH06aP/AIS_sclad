package com.example.aisstock.repository;

import com.example.aisstock.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);

    @Query("SELECT p FROM Product p WHERE LOWER(p.sku) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Product> searchBySkuOrNameContaining(@Param("q") String q);
}
