package com.example.aisstock.service;

import com.example.aisstock.model.Document;
import com.example.aisstock.model.DocumentItem;
import com.example.aisstock.model.StockItem;
import com.example.aisstock.model.StockTransaction;
import com.example.aisstock.model.StockTransactionType;
import com.example.aisstock.repository.StockTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StockTransactionService {
    private final StockTransactionRepository repository;

    public StockTransactionService(StockTransactionRepository repository) {
        this.repository = repository;
    }

    public List<StockTransaction> findAll() {
        return repository.findAll();
    }

    @Transactional
    public StockTransaction save(StockTransaction transaction) {
        return repository.save(transaction);
    }

    @Transactional
    public void recordDocumentMovement(Document document, DocumentItem item, StockItem stockItem, StockTransactionType type) {
        StockTransaction transaction = new StockTransaction();
        transaction.setTransactionType(type);
        transaction.setStockItem(stockItem);
        transaction.setQuantity(item.getQuantity());
        transaction.setLocation(item.getStorageLocation());
        transaction.setReference(document.getDocumentNumber() != null
                ? document.getDocumentNumber()
                : "DOC-" + document.getId());
        transaction.setNotes(document.getNotes());
        transaction.setCreatedBy(document.getCreatedBy());
        save(transaction);
    }
}
