package com.example.blog_website_springboot.Repository;

import com.example.blog_website_springboot.Model.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface BlogRepository extends JpaRepository<Blog, Long> {
    List<Blog> findByUsername(String username);
}
