package com.example.demo_bckend.repository;




import com.example.demo_bckend.model.TodoItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<TodoItem, Integer> {
}
