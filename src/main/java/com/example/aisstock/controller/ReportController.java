package com.example.aisstock.controller;

import com.example.aisstock.dto.MovementReportRow;
import com.example.aisstock.dto.ProductSummary;
import com.example.aisstock.service.ExcelExportService;
import com.example.aisstock.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final ExcelExportService excelExportService;

    public ReportController(ReportService reportService, ExcelExportService excelExportService) {
        this.reportService = reportService;
        this.excelExportService = excelExportService;
    }

    @GetMapping("/stock")
    public List<ProductSummary> getStockReport() {
        return reportService.getStockByProduct();
    }

    @GetMapping("/transactions")
    public List<MovementReportRow> getTransactionsReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportService.getMovements(from, to);
    }

    @GetMapping("/stock/export")
    public ResponseEntity<byte[]> exportStock() throws IOException {
        List<ProductSummary> items = reportService.getStockByProduct();
        byte[] data = excelExportService.exportStockBalanceReport(items);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=stock_report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    @GetMapping("/transactions/export")
    public ResponseEntity<byte[]> exportTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) throws IOException {
        List<MovementReportRow> movements = reportService.getMovements(from, to);
        byte[] data = excelExportService.exportMovementsReport(movements);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=movements_report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
}
