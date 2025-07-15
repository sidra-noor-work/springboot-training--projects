package com.example.demo_bckend.controller;

import com.example.demo_bckend.model.TodoItem;
import com.example.demo_bckend.repository.TodoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoRepository repo;

    public TodoController(TodoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<TodoItem> getAll() {
        return repo.findAll();
    }

    @PostMapping
    public TodoItem create(@RequestBody TodoItem item) {
        return repo.save(item);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        repo.deleteById(id);
    }
}
