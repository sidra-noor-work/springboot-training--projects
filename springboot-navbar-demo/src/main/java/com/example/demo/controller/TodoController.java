package com.example.demo.controller;

import com.example.demo.model.TodoItem;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final List<TodoItem> items = new ArrayList<>();
    private int nextId = 1;

    @GetMapping
    public List<TodoItem> getItems() {
        return items;
    }

    @PostMapping
    public void addItem(@RequestBody TodoItem item) {
        item.setId(nextId++);
        items.add(item);
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable int id) {
        items.removeIf(item -> item.getId() == id);
    }

    @PutMapping("/{id}")
    public void updateItem(@PathVariable int id, @RequestBody TodoItem updatedItem) {
        for (TodoItem item : items) {
            if (item.getId() == id) {
                item.setText(updatedItem.getText());
                break;
            }
        }
    }
}