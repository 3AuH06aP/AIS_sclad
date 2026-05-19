package com.example.aisstock.service;

import com.example.aisstock.model.*;
import com.example.aisstock.repository.DocumentRepository;
import com.example.aisstock.repository.DocumentItemRepository;
import com.example.aisstock.repository.ProductRepository;
import com.example.aisstock.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentItemRepository documentItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private StockService stockService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private StockTransactionService stockTransactionService;

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            return user.getUsername();
        }
        return principal.toString();
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    public List<Document> getDocumentsByType(Document.DocumentType documentType) {
        return documentRepository.findByDocumentType(documentType);
    }

    public List<Document> getDocumentsByStatus(Document.DocumentStatus status) {
        return documentRepository.findByStatus(status);
    }

    @Transactional
    public Document saveDocument(Document document) {
        // Attach existing warehouses to prevent TransientPropertyValueException
        if (document.getWarehouseFrom() != null && document.getWarehouseFrom().getId() != null) {
            document.setWarehouseFrom(warehouseRepository.findById(document.getWarehouseFrom().getId())
                    .orElseThrow(() -> new RuntimeException("Склад отправителя не найден")));
        }
        if (document.getWarehouseTo() != null && document.getWarehouseTo().getId() != null) {
            document.setWarehouseTo(warehouseRepository.findById(document.getWarehouseTo().getId())
                    .orElseThrow(() -> new RuntimeException("Склад назначения не найден")));
        }

        if (document.getItems() != null) {
            for (DocumentItem item : document.getItems()) {
                item.setDocument(document);
                // Attach existing products
                if (item.getProduct() != null && item.getProduct().getId() != null) {
                    item.setProduct(productRepository.findById(item.getProduct().getId())
                            .orElseThrow(() -> new RuntimeException("Товар не найден")));
                }
            }
        }
        
        Document saved = documentRepository.save(document);
        activityLogService.log(getCurrentUsername(), "SAVE_DOCUMENT", "Document #" + saved.getId() + " saved");
        return saved;
    }

    @Transactional
    public Document createReceiptDocument(Document document) {
        document.setDocumentType(Document.DocumentType.RECEIPT);
        return saveDraft(document);
    }

    @Transactional
    public Document createShipmentDocument(Document document) {
        document.setDocumentType(Document.DocumentType.SHIPMENT);
        return saveDraft(document);
    }

    @Transactional
    public Document createTransferDocument(Document document) {
        document.setDocumentType(Document.DocumentType.TRANSFER);
        return saveDraft(document);
    }

    @Transactional
    public Document createWriteOffDocument(Document document) {
        document.setDocumentType(Document.DocumentType.WRITE_OFF);
        return saveDraft(document);
    }

    private Document saveDraft(Document document) {
        document.setStatus(Document.DocumentStatus.DRAFT);
        document.setCreatedBy(getCurrentUsername());
        return saveDocument(document);
    }

    @Transactional
    public Document confirmDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Документ не найден: " + documentId));

        if (document.getStatus() == Document.DocumentStatus.CONFIRMED || 
            document.getStatus() == Document.DocumentStatus.COMPLETED) {
            return document;
        }

        if (document.getStatus() != Document.DocumentStatus.DRAFT) {
            throw new RuntimeException("Только ЧЕРНОВИКИ могут быть подтверждены. Текущий статус: " + document.getStatus());
        }

        for (DocumentItem item : document.getItems()) {
            processItem(document, item);
        }

        document.setStatus(Document.DocumentStatus.CONFIRMED);
        Document saved = documentRepository.save(document);
        activityLogService.log(getCurrentUsername(), "CONFIRM_DOCUMENT", "Document #" + saved.getId() + " confirmed. Stock updated.");
        return saved;
    }

    private void processItem(Document document, DocumentItem item) {
        try {
            switch (document.getDocumentType()) {
                case RECEIPT -> {
                    StockItem received = stockService.addStock(item.getProduct().getId(),
                            document.getWarehouseTo().getId(),
                            item.getQuantity(),
                            item.getStorageLocation(),
                            item.getBatch(),
                            null);
                    stockTransactionService.recordDocumentMovement(document, item, received, StockTransactionType.RECEIPT);
                    createTask(document, item, Task.TaskType.PUTAWAY, document.getWarehouseTo());
                }
                case SHIPMENT -> {
                    StockItem shipped = stockService.removeStock(item.getProduct().getId(),
                            document.getWarehouseFrom().getId(),
                            item.getQuantity(),
                            item.getStorageLocation(),
                            item.getBatch());
                    stockTransactionService.recordDocumentMovement(document, item, shipped, StockTransactionType.ISSUE);
                    createTask(document, item, Task.TaskType.PICKING, document.getWarehouseFrom());
                }
                case TRANSFER -> {
                    StockItem removed = stockService.removeStock(item.getProduct().getId(),
                            document.getWarehouseFrom().getId(),
                            item.getQuantity(),
                            item.getStorageLocation(),
                            item.getBatch());
                    stockTransactionService.recordDocumentMovement(document, item, removed, StockTransactionType.ISSUE);

                    StockItem added = stockService.addStock(item.getProduct().getId(),
                            document.getWarehouseTo().getId(),
                            item.getQuantity(),
                            item.getStorageLocation(),
                            item.getBatch(),
                            null);
                    stockTransactionService.recordDocumentMovement(document, item, added, StockTransactionType.RECEIPT);

                    createTask(document, item, Task.TaskType.PICKING, document.getWarehouseFrom());
                    createTask(document, item, Task.TaskType.PUTAWAY, document.getWarehouseTo());
                }
                case WRITE_OFF -> {
                    StockItem writtenOff = stockService.removeStock(item.getProduct().getId(),
                            document.getWarehouseFrom().getId(),
                            item.getQuantity(),
                            item.getStorageLocation(),
                            item.getBatch());
                    stockTransactionService.recordDocumentMovement(document, item, writtenOff, StockTransactionType.ISSUE);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка обработки товара " + item.getProduct().getSku() + ": " + e.getMessage());
        }
    }

    private void createTask(Document document, DocumentItem item, Task.TaskType type, Warehouse warehouse) {
        Task task = new Task();
        task.setTaskType(type);
        task.setProduct(item.getProduct());
        task.setQuantity(item.getQuantity());
        task.setWarehouse(warehouse);
        task.setStorageLocation(item.getStorageLocation());
        task.setBatch(item.getBatch());
        task.setNotes((type == Task.TaskType.PUTAWAY ? "Размещение" : "Сборка") + 
                     " товара по документу #" + document.getId());
        
        if (type == Task.TaskType.PUTAWAY) {
            taskService.createPutawayTask(task);
        } else {
            taskService.createPickingTask(task);
        }
    }

    @Transactional
    public Document completeDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Документ не найден: " + documentId));
        
        if (document.getStatus() == Document.DocumentStatus.COMPLETED) {
            return document;
        }

        document.setStatus(Document.DocumentStatus.COMPLETED);
        Document saved = documentRepository.save(document);
        activityLogService.log(getCurrentUsername(), "COMPLETE_DOCUMENT", "Document #" + saved.getId() + " completed");
        return saved;
    }

    public void deleteDocument(Long id) {
        activityLogService.log(getCurrentUsername(), "DELETE_DOCUMENT", "Document #" + id + " deleted");
        documentRepository.deleteById(id);
    }
}
