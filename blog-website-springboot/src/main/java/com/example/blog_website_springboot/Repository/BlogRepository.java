package com.example.blog_website_springboot.Repository;



import com.example.blog_website_springboot.Model.Blog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogRepository extends JpaRepository<Blog, Long> {
}
