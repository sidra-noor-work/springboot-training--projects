package com.example.unit_testing;

import com.example.unit_testing.exception.NewsNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/news")
public class NewsController {

    @GetMapping("/{id}")
    public ResponseEntity<News> getNewsById(@PathVariable int id) {
        if (id == 123) {
            News news = new News(123, "title 123", "details 123", "reporter 123", LocalDateTime.now());
            return ResponseEntity.ok(news);
        } else {
            throw new NewsNotFoundException(id); 
        }
    }

    @GetMapping
    public List<News> getAllNews() {
        News news = new News(123, "title 123", "details 123", "reporter 123", LocalDateTime.now());
        return List.of(news);
    }
}
