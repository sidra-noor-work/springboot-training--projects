package com.example.blog_website_springboot;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@Controller
public class BlogController {


    private final BlogRepository blogRepository;
    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    public BlogController(BlogRepository blogRepository) {
        this.blogRepository = blogRepository;
    }

    // This ensures opening http://localhost:8080/ redirects to /login
    @GetMapping("/")
    public String homeRedirect() {
        return "redirect:/login";
    }

    @GetMapping("/create")
    public String showBlogs(Model model) {
        model.addAttribute("blog", new Blog());
        model.addAttribute("blogs", blogRepository.findAll());
        return "form";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("user", new AppUser());
        return "signup";
    }
    @PostMapping("/signup")
    public String signup(@ModelAttribute("user") AppUser user) {
        boolean registered = userService.register(user);
        return registered ? "redirect:/login" : "redirect:/signup?error";
    }
    @PostMapping("/save")
    public String saveBlog(@ModelAttribute Blog blog, Model model) {
        blogRepository.save(blog);
        model.addAttribute("blog", new Blog());
        model.addAttribute("blogs", blogRepository.findAll());
        System.out.println("Received POST: " + blog.getTitle());

        return "form";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpServletResponse response,
                        Model model) {
        String token = userService.authenticate(username, password);
        if (token != null) {
            System.out.println("SESSION TOKEN (JWT): " + token);
            response.setHeader("Authorization", "Bearer " + token);
            return "redirect:/create";
        } else {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }
    }

}


