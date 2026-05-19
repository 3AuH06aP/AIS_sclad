package com.example.aisstock.service;

import com.example.aisstock.model.Product;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelImportService {
    private final ProductService productService;

    public ExcelImportService(ProductService productService) {
        this.productService = productService;
    }

    public List<Product> importProducts(MultipartFile file) throws Exception {
        List<Product> products = new ArrayList<>();
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header
                
                Cell skuCell = row.getCell(0);
                if (skuCell == null) continue;

                Product p = new Product();
                p.setSku(getCellValueAsString(skuCell));
                p.setName(getCellValueAsString(row.getCell(1)));
                p.setCategory(getCellValueAsString(row.getCell(2)));
                p.setBarcode(getCellValueAsString(row.getCell(3)));
                p.setUnit(getCellValueAsString(row.getCell(4)));
                
                products.add(productService.save(p));
            }
        }
        return products;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}
