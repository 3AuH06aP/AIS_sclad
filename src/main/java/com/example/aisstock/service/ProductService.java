package com.example.aisstock.service;

import com.example.aisstock.dto.ProductSummary;
import com.example.aisstock.model.Product;
import com.example.aisstock.repository.ProductRepository;
import com.example.aisstock.repository.StockItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final StockItemRepository stockItemRepository;

    public ProductService(ProductRepository productRepository, StockItemRepository stockItemRepository) {
        this.productRepository = productRepository;
        this.stockItemRepository = stockItemRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public List<ProductSummary> findSummary() {
        return productRepository.findAll().stream().map(this::toSummary).toList();
    }

    public List<ProductSummary> searchSummaries(String query) {
        return productRepository.searchBySkuOrNameContaining(query).stream()
                .map(this::toSummary)
                .toList();
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<ProductSummary> findSummaryById(Long id) {
        return productRepository.findById(id).map(this::toSummary);
    }

    private ProductSummary toSummary(Product product) {
        int quantity = stockItemRepository.findByProduct(product).stream()
                .mapToInt(item -> item.getQuantity() == null ? 0 : item.getQuantity())
                .sum();
        return new ProductSummary(
                product.getId(),
                product.getSku(),
                product.getBarcode(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getUnit(),
                product.getInventoryClass(),
                product.getTrackingMethod(),
                product.getMinQuantity(),
                quantity,
                product.getPurchasePrice(),
                product.getSalePrice()
        );
    }

    @Transactional
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }
}
