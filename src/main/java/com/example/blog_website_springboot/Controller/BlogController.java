package com.example.blog_website_springboot.Controller;

import com.example.blog_website_springboot.Model.AppUser;
import com.example.blog_website_springboot.Model.Blog;
import com.example.blog_website_springboot.Repository.BlogRepository;
import com.example.blog_website_springboot.JWT.JwtUtil;
import com.example.blog_website_springboot.Service.BlogService;
import com.example.blog_website_springboot.Service.UserService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "*") // Allow CORS for testing with Postman
public class BlogController {

    @Autowired
    private BlogService blogService;

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

    @PostMapping("/auth/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody AppUser user) {
        Map<String, Object> response = new HashMap<>();


        System.out.println("Received signup request: username = " + user.getUsername() + ", password = " + user.getPassword());

        try {
            boolean registered = userService.register(user);

            if (!registered) {
                response.put("success", false);
                response.put("message", "User already exists!");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            response.put("success", true);
            response.put("message", "User registered successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AppUser request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            AppUser dummyUser = new AppUser();
            dummyUser.setUsername(userDetails.getUsername());
            dummyUser.setRole(userDetails.getAuthorities().iterator().next().getAuthority());

            String token = jwtUtil.generateToken(dummyUser);

            response.put("success", true);
            response.put("message", "Login successful");
            response.put("token", token);
            response.put("username", userDetails.getUsername());
            response.put("role", dummyUser.getRole());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    // Blog CRUD endpoints
    @GetMapping("/blogs")
    public ResponseEntity<Map<String, Object>> getAllBlogs(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (!isAuthenticated(authentication)) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            List<Blog> blogs = blogRepository.findAll();
            response.put("success", true);
            response.put("data", blogs);
            response.put("count", blogs.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to fetch blogs: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/blogs/{id}")
    public ResponseEntity<Map<String, Object>> getBlogById(@PathVariable Long id, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (!isAuthenticated(authentication)) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            Optional<Blog> blog = blogRepository.findById(id);

            if (blog.isPresent()) {
                response.put("success", true);
                response.put("data", blog.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Blog not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to fetch blog: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/blogs")
    public ResponseEntity<Map<String, Object>> createBlog(@RequestBody Blog blog, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (!isAuthenticated(authentication)) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            // Validate input
            if (blog.getTitle() == null || blog.getTitle().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Blog title is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (blog.getContent() == null || blog.getContent().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Blog content is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Blog savedBlog = blogRepository.save(blog);
            response.put("success", true);
            response.put("message", "Blog created successfully");
            response.put("data", savedBlog);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create blog: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/blogs/{id}")
    public ResponseEntity<Map<String, Object>> updateBlog(@PathVariable Long id, @RequestBody Blog blog, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (!isAuthenticated(authentication)) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            Optional<Blog> existingBlog = blogRepository.findById(id);

            if (!existingBlog.isPresent()) {
                response.put("success", false);
                response.put("message", "Blog not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Validate input
            if (blog.getTitle() == null || blog.getTitle().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Blog title is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (blog.getContent() == null || blog.getContent().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Blog content is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            blog.setId(id); // Ensure the ID is set
            Blog updatedBlog = blogRepository.save(blog);

            response.put("success", true);
            response.put("message", "Blog updated successfully");
            response.put("data", updatedBlog);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update blog: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/blogs/{id}")
    public ResponseEntity<Map<String, Object>> deleteBlog(@PathVariable Long id, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (!isAuthenticated(authentication)) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            Optional<Blog> existingBlog = blogRepository.findById(id);

            if (!existingBlog.isPresent()) {
                response.put("success", false);
                response.put("message", "Blog not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            blogRepository.deleteById(id);

            response.put("success", true);
            response.put("message", "Blog deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete blog: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Helper method to check authentication
    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken);
    }

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Blog API is running");
        return ResponseEntity.ok(response);
    }
}