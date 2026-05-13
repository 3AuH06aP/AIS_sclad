package com.example.aisstock.repository;

import com.example.aisstock.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Warehouse> findByName(String name);
}
