package com.example.aisstock.service;

import com.example.aisstock.model.SerialNumber;
import com.example.aisstock.repository.SerialNumberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SerialNumberService {

    @Autowired
    private SerialNumberRepository serialNumberRepository;

    public List<SerialNumber> getAllSerialNumbers() {
        return serialNumberRepository.findAll();
    }

    public Optional<SerialNumber> getSerialNumberById(Long id) {
        return serialNumberRepository.findById(id);
    }

    public Optional<SerialNumber> getSerialNumberByNumber(String serialNumber) {
        return serialNumberRepository.findBySerialNumber(serialNumber);
    }

    public List<SerialNumber> getSerialNumbersByProduct(Long productId) {
        return serialNumberRepository.findByProductId(productId);
    }

    public List<SerialNumber> getSerialNumbersByStockItem(Long stockItemId) {
        return serialNumberRepository.findByStockItemId(stockItemId);
    }

    public List<SerialNumber> getSerialNumbersByStatus(String status) {
        return serialNumberRepository.findByStatus(status);
    }

    public SerialNumber saveSerialNumber(SerialNumber serialNumber) {
        return serialNumberRepository.save(serialNumber);
    }

    public void deleteSerialNumber(Long id) {
        serialNumberRepository.deleteById(id);
    }
}