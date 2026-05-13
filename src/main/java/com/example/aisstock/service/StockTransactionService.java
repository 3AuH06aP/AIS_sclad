package com.example.aisstock.service;

import com.example.aisstock.model.StockTransaction;
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
}
