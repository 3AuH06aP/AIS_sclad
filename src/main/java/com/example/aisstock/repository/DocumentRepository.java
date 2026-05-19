package com.example.aisstock.repository;

import com.example.aisstock.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByDocumentType(Document.DocumentType documentType);
    List<Document> findByStatus(Document.DocumentStatus status);
    List<Document> findByCreatedBy(String createdBy);
    List<Document> findByDocumentNumber(String documentNumber);
}