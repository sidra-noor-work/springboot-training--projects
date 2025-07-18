package com.example.blog_website_springboot;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@Controller
public class BlogController {

    private final BlogRepository blogRepository;

    public BlogController(BlogRepository blogRepository) {
        this.blogRepository = blogRepository;
    }

    // This ensures opening http://localhost:8080/ redirects to /login
    @GetMapping("/")
    public String homeRedirect() {
        return "redirect:/login";
    }

    @PostMapping("/create")
    public String showBlogs(Model model) {
        model.addAttribute("blog", new Blog());
        model.addAttribute("blogs", blogRepository.findAll());
        return "form"; // must match form.html
    }


    @PostMapping("/save")
    public String saveBlog(@ModelAttribute Blog blog, Model model) {
        blogRepository.save(blog);
        model.addAttribute("blog", new Blog());
        model.addAttribute("blogs", blogRepository.findAll());
        System.out.println("Received POST: " + blog.getTitle());

        return "form";
    }


    @GetMapping("/login")
    public String login() {
        return "login";
    }
}


