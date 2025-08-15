package com.example.blogwebsitespringboot.service;

import com.example.blogwebsitespringboot.model.Blog;
import com.example.blogwebsitespringboot.repository.BlogRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BlogService {

    private final BlogRepository blogRepository;

    public BlogService(final BlogRepository blogRepository) {
        this.blogRepository = blogRepository;
    }

    public List<Blog> getAll() {
        return blogRepository.findAll();
    }

    public Blog get(final Long blogId) {
        return blogRepository.findById(blogId).orElse(null);
    }

    public Blog save(final Blog blog) {
        return blogRepository.save(blog);
    }

    public void delete(final Long blogId) {
        blogRepository.deleteById(blogId);
    }
}
