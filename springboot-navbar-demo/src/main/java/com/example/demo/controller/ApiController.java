package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ApiController {
    @GetMapping("/api/items")
    public List<String> getItems() {
        return List.of("Item A", "Item B", "Item C");
    }
}