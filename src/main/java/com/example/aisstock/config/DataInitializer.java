package com.example.aisstock.config;

import com.example.aisstock.model.Product;
import com.example.aisstock.model.StockItem;
import com.example.aisstock.model.User;
import com.example.aisstock.model.UserRole;
import com.example.aisstock.model.Warehouse;
import com.example.aisstock.repository.ProductRepository;
import com.example.aisstock.repository.WarehouseRepository;
import com.example.aisstock.service.StockService;
import com.example.aisstock.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Order(1)
public class DataInitializer implements ApplicationRunner {
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockService stockService;
    private final UserService userService;

    public DataInitializer(ProductRepository productRepository,
                           WarehouseRepository warehouseRepository,
                           StockService stockService,
                           UserService userService) {
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.stockService = stockService;
        this.userService = userService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Simple initialization without password re-hashing
        if (userService.findByUsername("admin").isEmpty()) {
            createUser("admin", "admin", UserRole.ADMIN);
        }
        if (userService.findByUsername("keeper").isEmpty()) {
            createUser("keeper", "keeper", UserRole.STOREKEEPER);
        }

        if (productRepository.count() == 0 && warehouseRepository.count() == 0) {
            Warehouse mainWarehouse = createWarehouse("Главный склад", "Центр логистики");
            Warehouse secondaryWarehouse = createWarehouse("Резервный склад", "Административный корпус");

            Product laptop = createProduct("LAP-100", "Ноутбук бизнес-класса", "Электроника", "шт", 5, new BigDecimal("750.00"), new BigDecimal("980.00"), "Ультра-легкий ноутбук для офиса");
            Product chair = createProduct("CHR-250", "Офисное кресло", "Мебель", "шт", 10, new BigDecimal("85.00"), new BigDecimal("130.00"), "Эргономичное кресло для сотрудников");
            Product cable = createProduct("CBL-500", "Сетевой кабель 5м", "Аксессуары", "шт", 20, new BigDecimal("4.50"), new BigDecimal("7.20"), "Кабель RJ45 для подключения сети");

            createStockItem(laptop, mainWarehouse, 42);
            createStockItem(chair, mainWarehouse, 12);
            createStockItem(chair, secondaryWarehouse, 8);
            createStockItem(cable, secondaryWarehouse, 6);
        }
    }

    private void createUser(String username, String password, UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // Will be saved as plain text
        user.setRole(role);
        if ("admin".equals(username)) {
            user.setFullName("Иван Иванов");
        } else if ("keeper".equals(username)) {
            user.setFullName("Пётр Кладовщиков");
        }
        userService.saveDirectly(user);
    }

    private Warehouse createWarehouse(String name, String location) {
        Warehouse warehouse = new Warehouse();
        warehouse.setName(name);
        warehouse.setLocation(location);
        return warehouseRepository.save(warehouse);
    }

    private Product createProduct(String sku, String name, String category, String unit, int minQuantity, BigDecimal purchasePrice, BigDecimal salePrice, String description) {
        Product product = new Product();
        product.setSku(sku);
        product.setName(name);
        product.setCategory(category);
        product.setUnit(unit);
        product.setMinQuantity(minQuantity);
        product.setPurchasePrice(purchasePrice);
        product.setSalePrice(salePrice);
        product.setDescription(description);
        return productRepository.save(product);
    }

    private StockItem createStockItem(Product product, Warehouse warehouse, int quantity) {
        StockItem item = new StockItem();
        item.setProduct(product);
        item.setWarehouse(warehouse);
        item.setQuantity(quantity);
        return stockService.save(item);
    }
}
