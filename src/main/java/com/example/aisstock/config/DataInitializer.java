package com.example.aisstock.config;

import com.example.aisstock.model.*;
import com.example.aisstock.repository.*;
import com.example.aisstock.service.StockService;
import com.example.aisstock.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Order(1)
public class DataInitializer implements ApplicationRunner {
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockService stockService;
    private final UserService userService;
    private final DocumentRepository documentRepository;
    private final StockTransactionRepository transactionRepository;

    public DataInitializer(ProductRepository productRepository,
                           WarehouseRepository warehouseRepository,
                           StockService stockService,
                           UserService userService,
                           DocumentRepository documentRepository,
                           StockTransactionRepository transactionRepository) {
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.stockService = stockService;
        this.userService = userService;
        this.documentRepository = documentRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // 1. Инициализация пользователей
        if (userService.findByUsername("admin").isEmpty()) {
            createUser("admin", "admin", UserRole.ADMIN, "Иван Иванов");
        }
        if (userService.findByUsername("keeper").isEmpty()) {
            createUser("keeper", "keeper", UserRole.STOREKEEPER, "Пётр Кладовщиков");
        }

        // 2. Инициализация справочников и остатков, если пусто
        if (productRepository.count() == 0 && warehouseRepository.count() == 0) {
            Warehouse mainWarehouse = createWarehouse("Главный склад", "ул. Промышленная, 10");
            Warehouse retailWarehouse = createWarehouse("Склад магазина", "пр. Ленина, 45");
            Warehouse transitWarehouse = createWarehouse("Транзитная зона", "Перрон №3");

            // Список категорий для разнообразия
            String[] categories = {"Электроника", "Периферия", "Сетевое оборудование", "Оргтехника", "Расходные материалы"};
            
            List<Product> products = new ArrayList<>();
            
            // Генерируем ~20 товаров
            products.add(createProduct("MON-27-IPS", "Монитор 27\" IPS", categories[0], "шт", 5, 12000, 18500, "Высокое разрешение"));
            products.add(createProduct("KBD-MECH", "Механическая клавиатура", categories[1], "шт", 10, 3500, 5200, "RGB подсветка"));
            products.add(createProduct("MS-LOGI", "Беспроводная мышь", categories[1], "шт", 15, 1500, 2800, "Эргономичная"));
            products.add(createProduct("RTR-WIFI6", "Wi-Fi Роутер AX3000", categories[2], "шт", 8, 4200, 6900, "Новое поколение"));
            products.add(createProduct("PRN-LSR", "Лазерный принтер", categories[3], "шт", 3, 15000, 22000, "Ч/Б печать"));
            products.add(createProduct("LPT-PRO-15", "Ноутбук 15.6\" i7", categories[0], "шт", 5, 65000, 89000, "Для работы"));
            products.add(createProduct("TBL-TAB-10", "Планшет 10\" OLED", categories[0], "шт", 7, 25000, 38000, "Тонкий корпус"));
            products.add(createProduct("CBL-HDMI-2", "Кабель HDMI 2.0 2м", categories[2], "шт", 20, 300, 850, "Позолоченные контакты"));
            products.add(createProduct("CRTR-BLK", "Картридж черный", categories[4], "шт", 12, 1200, 2100, "Оригинальный"));
            products.add(createProduct("PPR-A4-500", "Бумага A4 (500л)", categories[4], "уп", 50, 450, 750, "Белая, 80г/м2"));
            products.add(createProduct("UPS-800", "ИБП 800VA", categories[0], "шт", 4, 4500, 7200, "Защита питания"));
            products.add(createProduct("HDD-2TB", "Жесткий диск 2TB", categories[0], "шт", 10, 5000, 7800, "SATA III"));
            products.add(createProduct("SSD-500G", "SSD Накопитель 500GB", categories[0], "шт", 15, 3200, 5500, "NVMe"));
            products.add(createProduct("SWT-8P", "Коммутатор 8 портов", categories[2], "шт", 6, 1800, 3100, "Гигабитный"));
            products.add(createProduct("WEB-CAM-HD", "Веб-камера Full HD", categories[1], "шт", 10, 2200, 4300, "Автофокус"));
            products.add(createProduct("HDS-PRO", "Наушники с микрофоном", categories[1], "шт", 12, 2800, 4900, "Шумоподавление"));
            products.add(createProduct("USB-DRV-64", "Флеш-диск 64GB", categories[0], "шт", 30, 600, 1200, "USB 3.1"));
            products.add(createProduct("SPK-20", "Колонки 2.0", categories[1], "шт", 8, 1400, 2600, "Деревянный корпус"));
            products.add(createProduct("SCN-DSK", "Сканер планшетный", categories[3], "шт", 2, 8500, 13400, "Высокая скорость"));
            products.add(createProduct("CLN-KIT", "Набор для чистки", categories[4], "шт", 25, 250, 550, "Спрей и салфетка"));

            Random rng = new Random();
            
            // Распределяем начальные остатки
            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                Warehouse target = (i % 2 == 0) ? mainWarehouse : retailWarehouse;
                int qty = 10 + rng.nextInt(40);
                String loc = "Z-" + (i + 1) + "-" + (rng.nextInt(5) + 1);
                createStock(p, target, qty, loc, "BATCH-2024-V" + (i + 1));
            }

            // Создаем пачку документов
            createDemoDocument(Document.DocumentType.RECEIPT, null, mainWarehouse, "REC-START", "Начальный завоз", List.of(
                    createItem(products.get(0), 10, products.get(0).getPurchasePrice(), "A-1-1"),
                    createItem(products.get(1), 15, products.get(1).getPurchasePrice(), "B-2-2"),
                    createItem(products.get(4), 5, products.get(4).getPurchasePrice(), "C-1-1")
            ), Document.DocumentStatus.COMPLETED);

            createDemoDocument(Document.DocumentType.SHIPMENT, mainWarehouse, null, "SHP-001", "Продажа корпоративному клиенту", List.of(
                    createItem(products.get(5), 2, products.get(5).getSalePrice(), "A-5-1"),
                    createItem(products.get(10), 1, products.get(10).getSalePrice(), "B-1-1")
            ), Document.DocumentStatus.COMPLETED);

            createDemoDocument(Document.DocumentType.TRANSFER, mainWarehouse, retailWarehouse, "TRF-001", "Пополнение запаса в магазине", List.of(
                    createItem(products.get(12), 10, BigDecimal.ZERO, "S-1-1"),
                    createItem(products.get(16), 20, BigDecimal.ZERO, "S-2-2")
            ), Document.DocumentStatus.COMPLETED);
            
            createDemoDocument(Document.DocumentType.RECEIPT, null, mainWarehouse, "REC-002", "Поставка расходников", List.of(
                    createItem(products.get(8), 50, products.get(8).getPurchasePrice(), "R-1-1"),
                    createItem(products.get(9), 100, products.get(9).getPurchasePrice(), "R-2-1")
            ), Document.DocumentStatus.COMPLETED);
            
            createDemoDocument(Document.DocumentType.WRITE_OFF, retailWarehouse, null, "WO-001", "Списание брака", List.of(
                    createItem(products.get(2), 2, products.get(2).getPurchasePrice(), "SHOWCASE-1")
            ), Document.DocumentStatus.COMPLETED);
        }
    }

    private void createStock(Product product, Warehouse warehouse, int quantity, String location, String batch) {
        stockService.addStock(product.getId(), warehouse.getId(), quantity, location, batch, null);
        
        StockTransaction tx = new StockTransaction();
        tx.setStockItem(stockService.findByProductAndWarehouse(product, warehouse).orElse(null));
        tx.setQuantity(quantity);
        tx.setTransactionType(StockTransactionType.RECEIPT);
        tx.setCreatedBy("system");
        tx.setLocation(location);
        tx.setNotes("Начальный ввод остатков");
        transactionRepository.save(tx);
    }

    private void createUser(String username, String password, UserRole role, String fullName) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        try {
            user.getClass().getMethod("setFullName", String.class).invoke(user, fullName);
        } catch (Exception e) { /* skip if not exists */ }
        userService.saveDirectly(user);
    }

    private Warehouse createWarehouse(String name, String location) {
        Warehouse warehouse = new Warehouse();
        warehouse.setName(name);
        warehouse.setLocation(location);
        return warehouseRepository.save(warehouse);
    }

    private Product createProduct(String sku, String name, String category, String unit, int min, double pur, double sale, String desc) {
        Product product = new Product();
        product.setSku(sku);
        product.setName(name);
        product.setCategory(category);
        product.setUnit(unit);
        product.setMinQuantity(min);
        product.setPurchasePrice(BigDecimal.valueOf(pur));
        product.setSalePrice(BigDecimal.valueOf(sale));
        product.setDescription(desc);
        return productRepository.save(product);
    }

    private void createDemoDocument(Document.DocumentType type, Warehouse from, Warehouse to, String num, String notes, List<DocumentItem> items, Document.DocumentStatus status) {
        Document doc = new Document();
        doc.setDocumentType(type);
        doc.setWarehouseFrom(from);
        doc.setWarehouseTo(to);
        doc.setDocumentNumber(num);
        doc.setNotes(notes);
        doc.setStatus(status);
        doc.setCreatedBy("admin");
        
        doc.setItems(new ArrayList<>());
        for (DocumentItem item : items) {
            item.setDocument(doc);
            doc.getItems().add(item);
        }
        documentRepository.save(doc);
    }

    private DocumentItem createItem(Product product, int qty, BigDecimal price, String loc) {
        DocumentItem item = new DocumentItem();
        item.setProduct(product);
        item.setQuantity(qty);
        item.setUnitPrice(price);
        item.setStorageLocation(loc);
        return item;
    }
}
