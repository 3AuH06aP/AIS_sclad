package com.example.aisstock.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ApiStatusController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of("status", "AIS Stock backend is running", "frontend", "http://localhost:5173");
    }

    @GetMapping("/api/status")
    public Map<String, String> status() {
        return Map.of("status", "ok");
    }
}
