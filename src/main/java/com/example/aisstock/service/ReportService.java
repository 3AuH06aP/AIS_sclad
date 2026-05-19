package com.example.aisstock.service;

import com.example.aisstock.dto.MovementReportRow;
import com.example.aisstock.dto.ProductSummary;
import com.example.aisstock.model.StockTransaction;
import com.example.aisstock.model.StockTransactionType;
import com.example.aisstock.repository.StockTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final ProductService productService;
    private final StockTransactionRepository transactionRepository;

    public ReportService(ProductService productService, StockTransactionRepository transactionRepository) {
        this.productService = productService;
        this.transactionRepository = transactionRepository;
    }

    public List<ProductSummary> getStockByProduct() {
        return productService.findSummary();
    }

    public List<MovementReportRow> getMovements(LocalDate from, LocalDate to) {
        List<StockTransaction> transactions;
        if (from != null && to != null) {
            OffsetDateTime fromInstant = from.atStartOfDay().atOffset(ZoneOffset.UTC);
            OffsetDateTime toInstant = to.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);
            transactions = transactionRepository.findForReportBetween(fromInstant, toInstant);
        } else if (from != null) {
            transactions = transactionRepository.findForReportFrom(
                    from.atStartOfDay().atOffset(ZoneOffset.UTC));
        } else if (to != null) {
            transactions = transactionRepository.findForReportTo(
                    to.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC));
        } else {
            transactions = transactionRepository.findAllForReport();
        }

        return transactions.stream()
                .map(this::toMovementRow)
                .toList();
    }

    private MovementReportRow toMovementRow(StockTransaction tx) {
        StockTransactionType type = tx.getTransactionType();
        String direction = type != null && type.isOutgoing() ? "OUT" : "IN";
        var stockItem = tx.getStockItem();
        var product = stockItem.getProduct();
        var warehouse = stockItem.getWarehouse();

        return new MovementReportRow(
                tx.getId(),
                tx.getCreatedAt(),
                direction,
                type != null ? type.name() : "",
                product.getSku(),
                product.getName(),
                warehouse.getName(),
                tx.getQuantity(),
                tx.getCreatedBy(),
                tx.getReference(),
                tx.getNotes()
        );
    }
}
