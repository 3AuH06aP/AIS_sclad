package com.example.aisstock.repository;

import com.example.aisstock.model.StockTransaction;
import com.example.aisstock.model.StockTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

    @Query("""
            SELECT DISTINCT t FROM StockTransaction t
            JOIN FETCH t.stockItem si
            JOIN FETCH si.product
            JOIN FETCH si.warehouse
            ORDER BY t.createdAt DESC
            """)
    List<StockTransaction> findAllForReport();

    @Query("""
            SELECT DISTINCT t FROM StockTransaction t
            JOIN FETCH t.stockItem si
            JOIN FETCH si.product
            JOIN FETCH si.warehouse
            WHERE t.createdAt >= :from AND t.createdAt <= :to
            ORDER BY t.createdAt DESC
            """)
    List<StockTransaction> findForReportBetween(
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);

    @Query("""
            SELECT DISTINCT t FROM StockTransaction t
            JOIN FETCH t.stockItem si
            JOIN FETCH si.product
            JOIN FETCH si.warehouse
            WHERE t.createdAt >= :from
            ORDER BY t.createdAt DESC
            """)
    List<StockTransaction> findForReportFrom(@Param("from") OffsetDateTime from);

    @Query("""
            SELECT DISTINCT t FROM StockTransaction t
            JOIN FETCH t.stockItem si
            JOIN FETCH si.product
            JOIN FETCH si.warehouse
            WHERE t.createdAt <= :to
            ORDER BY t.createdAt DESC
            """)
    List<StockTransaction> findForReportTo(@Param("to") OffsetDateTime to);

    @Query("""
            SELECT COUNT(t) FROM StockTransaction t
            WHERE t.transactionType IN :types
            AND t.createdAt >= :from
            AND t.createdAt < :to
            """)
    long countByTypesAndCreatedAtRange(
            @Param("types") Collection<StockTransactionType> types,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);
}
