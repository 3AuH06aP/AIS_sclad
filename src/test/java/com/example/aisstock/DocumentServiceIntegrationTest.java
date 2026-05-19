package com.example.aisstock;

import com.example.aisstock.model.*;
import com.example.aisstock.repository.ProductRepository;
import com.example.aisstock.repository.WarehouseRepository;
import com.example.aisstock.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class DocumentServiceIntegrationTest {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Test
    public void testCreateReceiptDocumentWithItems() {
        // Prepare data
        Warehouse warehouse = new Warehouse();
        warehouse.setName("Test Warehouse");
        warehouse = warehouseRepository.save(warehouse);

        Product product = new Product();
        product.setSku("TEST-SKU-1");
        product.setName("Test Product");
        product.setPurchasePrice(BigDecimal.TEN);
        product = productRepository.save(product);

        Document document = new Document();
        document.setWarehouseTo(warehouse);
        document.setDocumentNumber("DOC-001");
        
        List<DocumentItem> items = new ArrayList<>();
        DocumentItem item = new DocumentItem();
        item.setProduct(product);
        item.setQuantity(10);
        items.add(item);
        
        document.setItems(items);

        // Execute
        Document saved = documentService.createReceiptDocument(document);

        // Verify
        assertNotNull(saved.getId());
        assertEquals(Document.DocumentStatus.DRAFT, saved.getStatus());
        assertEquals(1, saved.getItems().size());
        assertNotNull(saved.getItems().get(0).getDocument(), "Document reference in item must not be null");
        assertEquals(saved.getId(), saved.getItems().get(0).getDocument().getId());
    }
}
