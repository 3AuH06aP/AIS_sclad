package com.example.aisstock.controller;

import com.example.aisstock.dto.ProductSummary;
import com.example.aisstock.model.Product;
import com.example.aisstock.service.ProductService;
import com.example.aisstock.service.ExcelImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    private final ExcelImportService excelImportService;

    public ProductController(ProductService productService, ExcelImportService excelImportService) {
        this.productService = productService;
        this.excelImportService = excelImportService;
    }

    @GetMapping
    public List<Product> list() {
        return productService.findAll();
    }

    @GetMapping("/summary")
    public List<ProductSummary> summary() {
        return productService.findSummary();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> get(@PathVariable Long id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Product> create(@Validated @RequestBody Product product) {
        return ResponseEntity.ok(productService.save(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id, @Validated @RequestBody Product product) {
        return productService.findById(id)
                .map(existing -> {
                    existing.setSku(product.getSku());
                    existing.setBarcode(product.getBarcode());
                    existing.setName(product.getName());
                    existing.setDescription(product.getDescription());
                    existing.setCategory(product.getCategory());
                    existing.setUnit(product.getUnit());
                    existing.setMinQuantity(product.getMinQuantity());
                    existing.setPurchasePrice(product.getPurchasePrice());
                    existing.setSalePrice(product.getSalePrice());
                    existing.setInventoryClass(product.getInventoryClass());
                    existing.setTrackingMethod(product.getTrackingMethod());
                    return ResponseEntity.ok(productService.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/import")
    public ResponseEntity<List<Product>> importExcel(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(excelImportService.importProducts(file));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
