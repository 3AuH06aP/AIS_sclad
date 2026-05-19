package com.example.aisstock.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 64)
    @Column(nullable = false, unique = true)
    private String sku;

    @Size(max = 64)
    private String barcode;

    @NotBlank
    @Size(max = 128)
    @Column(nullable = false)
    private String name;

    @Size(max = 128)
    private String category;

    @Size(max = 32)
    private String unit;

    @Size(max = 32)
    private String inventoryClass;

    @Size(max = 32)
    private String trackingMethod;

    @Min(0)
    private Integer minQuantity = 0;

    @Column(precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal salePrice;

    @Size(max = 512)
    private String description;

    public Product() {
    }

    public Product(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getInventoryClass() {
        return inventoryClass;
    }

    public void setInventoryClass(String inventoryClass) {
        this.inventoryClass = inventoryClass;
    }

    public String getTrackingMethod() {
        return trackingMethod;
    }

    public void setTrackingMethod(String trackingMethod) {
        this.trackingMethod = trackingMethod;
    }

    public Integer getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(Integer minQuantity) {
        this.minQuantity = minQuantity;
    }

    public java.math.BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(java.math.BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public java.math.BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(java.math.BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
