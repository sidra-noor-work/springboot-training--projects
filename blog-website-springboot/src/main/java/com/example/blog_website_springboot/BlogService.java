package com.example.blog_website_springboot;



import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BlogService {

    private final BlogRepository repo;

    public BlogService(BlogRepository repo) {
        this.repo = repo;
    }

    public List<Blog> getAll() {
        return repo.findAll();
    }

    public Blog get(Long id) {
        return repo.findById(id).orElse(null);
    }

    public Blog save(Blog blog) {
        return repo.save(blog);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}

