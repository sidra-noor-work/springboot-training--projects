package com.example.blogwebsitespringboot.repository;

import com.example.blogwebsitespringboot.model.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface BlogRepository extends JpaRepository<Blog, Long> {
    List<Blog> findByUsername(String username);
}
