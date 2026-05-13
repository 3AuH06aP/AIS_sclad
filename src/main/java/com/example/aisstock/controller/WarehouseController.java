package com.example.aisstock.controller;

import com.example.aisstock.model.Warehouse;
import com.example.aisstock.service.ActivityLogService;
import com.example.aisstock.service.WarehouseService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {
    private final WarehouseService warehouseService;
    private final ActivityLogService activityLogService;

    public WarehouseController(WarehouseService warehouseService, ActivityLogService activityLogService) {
        this.warehouseService = warehouseService;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    public List<Warehouse> list() {
        return warehouseService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Warehouse> get(@PathVariable Long id) {
        return warehouseService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Warehouse> create(@RequestHeader(value = "X-User-Name", defaultValue = "anonymous") String currentUser,
                                            @Validated @RequestBody Warehouse warehouse) {
        Warehouse saved = warehouseService.save(warehouse);
        activityLogService.log(currentUser, "create_warehouse", "Created warehouse " + saved.getName() + " (" + saved.getLocation() + ")");
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Warehouse> update(@RequestHeader(value = "X-User-Name", defaultValue = "anonymous") String currentUser,
                                           @PathVariable Long id, @Validated @RequestBody Warehouse warehouse) {
        return warehouseService.findById(id)
                .map(existing -> {
                    existing.setName(warehouse.getName());
                    existing.setLocation(warehouse.getLocation());
                    return ResponseEntity.ok(warehouseService.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        warehouseService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
