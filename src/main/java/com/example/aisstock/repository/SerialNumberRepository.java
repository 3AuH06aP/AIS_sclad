package com.example.aisstock.repository;

import com.example.aisstock.model.SerialNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SerialNumberRepository extends JpaRepository<SerialNumber, Long> {
    Optional<SerialNumber> findBySerialNumber(String serialNumber);
    List<SerialNumber> findByProductId(Long productId);
    List<SerialNumber> findByStockItemId(Long stockItemId);
    List<SerialNumber> findByStatus(String status);
}