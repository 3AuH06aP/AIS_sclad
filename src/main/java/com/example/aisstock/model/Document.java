package com.example.aisstock.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Size(max = 64)
    private String documentNumber;

    @Size(max = 128)
    private String reference; // external reference, like order number

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_from_id")
    private Warehouse warehouseFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_to_id")
    private Warehouse warehouseTo;

    @Size(max = 128)
    private String createdBy;

    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Size(max = 256)
    private String notes;

    @Enumerated(EnumType.STRING)
    private DocumentStatus status = DocumentStatus.DRAFT;

    @JsonManagedReference
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DocumentItem> items;

    public enum DocumentType {
        RECEIPT,    // Приёмка
        SHIPMENT,   // Отгрузка
        TRANSFER,   // Перемещение
        WRITE_OFF   // Списание
    }

    public enum DocumentStatus {
        DRAFT,
        CONFIRMED,
        COMPLETED,
        CANCELLED
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Warehouse getWarehouseFrom() {
        return warehouseFrom;
    }

    public void setWarehouseFrom(Warehouse warehouseFrom) {
        this.warehouseFrom = warehouseFrom;
    }

    public Warehouse getWarehouseTo() {
        return warehouseTo;
    }

    public void setWarehouseTo(Warehouse warehouseTo) {
        this.warehouseTo = warehouseTo;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public List<DocumentItem> getItems() {
        return items;
    }

    public void setItems(List<DocumentItem> items) {
        this.items = items;
    }
}
