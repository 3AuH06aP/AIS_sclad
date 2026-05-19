package com.example.aisstock.controller;

import com.example.aisstock.model.Document;
import com.example.aisstock.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public List<Document> getAllDocuments() {
        return documentService.getAllDocuments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id) {
        return documentService.getDocumentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{documentType}")
    public List<Document> getDocumentsByType(@PathVariable Document.DocumentType documentType) {
        return documentService.getDocumentsByType(documentType);
    }

    @GetMapping("/status/{status}")
    public List<Document> getDocumentsByStatus(@PathVariable Document.DocumentStatus status) {
        return documentService.getDocumentsByStatus(status);
    }

    @PostMapping("/receipt")
    public ResponseEntity<?> createReceiptDocument(@RequestBody Document document) {
        return createDocumentSafe(() -> documentService.createReceiptDocument(document));
    }

    @PostMapping("/shipment")
    public ResponseEntity<?> createShipmentDocument(@RequestBody Document document) {
        return createDocumentSafe(() -> documentService.createShipmentDocument(document));
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> createTransferDocument(@RequestBody Document document) {
        return createDocumentSafe(() -> documentService.createTransferDocument(document));
    }

    @PostMapping("/write-off")
    public ResponseEntity<?> createWriteOffDocument(@RequestBody Document document) {
        return createDocumentSafe(() -> documentService.createWriteOffDocument(document));
    }

    private ResponseEntity<?> createDocumentSafe(java.util.function.Supplier<Document> action) {
        try {
            return ResponseEntity.ok(action.get());
        } catch (Exception e) {
            logger.error("Error creating document: {}", e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public Document createDocument(@RequestBody Document document) {
        return documentService.saveDocument(document);
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<?> confirmDocument(@PathVariable Long id) {
        try {
            Document document = documentService.confirmDocument(id);
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            logger.error("Error confirming document {}: {}", id, e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeDocument(@PathVariable Long id) {
        try {
            Document document = documentService.completeDocument(id);
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            logger.error("Error completing document {}: {}", id, e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Document> updateDocument(@PathVariable Long id, @RequestBody Document document) {
        if (documentService.getDocumentById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        document.setId(id);
        return ResponseEntity.ok(documentService.saveDocument(document));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        if (documentService.getDocumentById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
