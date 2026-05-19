package com.example.aisstock.controller;

import com.example.aisstock.model.SerialNumber;
import com.example.aisstock.service.SerialNumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/serial-numbers")
public class SerialNumberController {

    @Autowired
    private SerialNumberService serialNumberService;

    @GetMapping
    public List<SerialNumber> getAllSerialNumbers() {
        return serialNumberService.getAllSerialNumbers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SerialNumber> getSerialNumberById(@PathVariable Long id) {
        return serialNumberService.getSerialNumberById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<SerialNumber> getSerialNumberByNumber(@RequestParam String serialNumber) {
        return serialNumberService.getSerialNumberByNumber(serialNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/product/{productId}")
    public List<SerialNumber> getSerialNumbersByProduct(@PathVariable Long productId) {
        return serialNumberService.getSerialNumbersByProduct(productId);
    }

    @PostMapping
    public SerialNumber createSerialNumber(@RequestBody SerialNumber serialNumber) {
        return serialNumberService.saveSerialNumber(serialNumber);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SerialNumber> updateSerialNumber(@PathVariable Long id, @RequestBody SerialNumber serialNumber) {
        if (!serialNumberService.getSerialNumberById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        serialNumber.setId(id);
        return ResponseEntity.ok(serialNumberService.saveSerialNumber(serialNumber));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSerialNumber(@PathVariable Long id) {
        if (!serialNumberService.getSerialNumberById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        serialNumberService.deleteSerialNumber(id);
        return ResponseEntity.noContent().build();
    }
}