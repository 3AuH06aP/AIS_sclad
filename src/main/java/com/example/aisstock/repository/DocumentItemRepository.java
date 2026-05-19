package com.example.aisstock.repository;

import com.example.aisstock.model.DocumentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentItemRepository extends JpaRepository<DocumentItem, Long> {
    List<DocumentItem> findByDocumentId(Long documentId);
    List<DocumentItem> findByProductId(Long productId);
}