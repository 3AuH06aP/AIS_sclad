package com.example.aisstock.service;

import com.example.aisstock.dto.MovementReportRow;
import com.example.aisstock.dto.ProductSummary;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportService {

    public byte[] exportStockBalanceReport(List<ProductSummary> items) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Остатки по товарам");

            Row header = sheet.createRow(0);
            String[] columns = {"Артикул", "Товар", "Категория", "Количество", "Ед. изм.", "Мин. остаток", "Ниже минимума"};
            writeHeader(workbook, header, columns);

            int rowIdx = 1;
            for (ProductSummary item : items) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(item.sku());
                row.createCell(1).setCellValue(item.name());
                row.createCell(2).setCellValue(item.category() != null ? item.category() : "");
                row.createCell(3).setCellValue(item.quantity() != null ? item.quantity() : 0);
                row.createCell(4).setCellValue(item.unit() != null ? item.unit() : "");
                row.createCell(5).setCellValue(item.minQuantity() != null ? item.minQuantity() : 0);
                row.createCell(6).setCellValue(isBelowMinimum(item) ? "Да" : "Нет");
            }

            autoSizeColumns(sheet, columns.length);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportMovementsReport(List<MovementReportRow> movements) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Движения");

            Row header = sheet.createRow(0);
            String[] columns = {"Дата", "Тип", "Операция", "Артикул", "Товар", "Склад", "Количество", "Пользователь", "Ссылка"};
            writeHeader(workbook, header, columns);

            int rowIdx = 1;
            for (MovementReportRow tx : movements) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(tx.transactionDate() != null ? tx.transactionDate().toString() : "");
                row.createCell(1).setCellValue("IN".equals(tx.type()) ? "Приход" : "Расход");
                row.createCell(2).setCellValue(tx.transactionType());
                row.createCell(3).setCellValue(tx.productSku());
                row.createCell(4).setCellValue(tx.productName());
                row.createCell(5).setCellValue(tx.warehouseName());
                row.createCell(6).setCellValue(tx.quantity() != null ? tx.quantity() : 0);
                row.createCell(7).setCellValue(tx.createdBy() != null ? tx.createdBy() : "");
                row.createCell(8).setCellValue(tx.reference() != null ? tx.reference() : "");
            }

            autoSizeColumns(sheet, columns.length);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private boolean isBelowMinimum(ProductSummary item) {
        int min = item.minQuantity() != null ? item.minQuantity() : 0;
        int qty = item.quantity() != null ? item.quantity() : 0;
        return qty <= min;
    }

    private void writeHeader(Workbook workbook, Row header, String[] columns) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(style);
        }
    }

    private void autoSizeColumns(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
