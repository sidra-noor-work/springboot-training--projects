package com.example.blogwebsitespringboot.controller;

import com.example.blogwebsitespringboot.model.AppUser;
import com.example.blogwebsitespringboot.model.Blog;
import com.example.blogwebsitespringboot.repository.BlogRepository;
import com.example.blogwebsitespringboot.service.BlogService;
import com.example.blogwebsitespringboot.service.UserService;
import com.example.blogwebsitespringboot.jwt.JwtUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for handling blog-related operations including CRUD operations,
 * user authentication, and JWT token management.
 */
@RestController
@RequestMapping("/")
@CrossOrigin(origins = "*")
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class BlogController {

  // Constants to eliminate duplicate string literals
  private static final String SUCCESS_KEY = "success";
  private static final String MESSAGE_KEY = "message";
  private static final String DATA_KEY = "data";
  private static final String AUTH_REQUIRED = "Authentication required";
  private static final String NOT_FOUND = "Blog not found";
  private static final String TITLE_REQUIRED = "Blog title is required";
  private static final String CONTENT_REQUIRED = "Blog content is required";

  private final SecurityContextLogoutHandler logoutHandler;
  private final BlogService blogService;
  private final BlogRepository blogRepository;
  private final UserService userService;
  private final AuthenticationManager authManager; // Shortened variable name
  private final JwtUtil jwtUtil;

  /**
   * Constructor for BlogController with dependency injection.
   */
  @SuppressFBWarnings(
          value = "EI_EXPOSE_REP2",
          justification = "Spring-managed service beans are intentionally stored by reference"
  )
  public BlogController(
          final BlogService blogService,
          final BlogRepository blogRepository,
          final UserService userService,
          final AuthenticationManager authManager,
          final JwtUtil jwtUtil,
          final SecurityContextLogoutHandler logoutHandler) {
    this.blogService = blogService;
    this.blogRepository = blogRepository;
    this.userService = userService;
    this.authManager = authManager;
    this.jwtUtil = jwtUtil;
    this.logoutHandler = logoutHandler;
  }

  /**
   * Handles user registration.
   */
  @PostMapping("/auth/signup")
  public ResponseEntity<Map<String, Object>> signup(final @RequestBody AppUser user) {
    final Map<String, Object> response = new HashMap<>();
    ResponseEntity<Map<String, Object>> result;

    try {
      final boolean registered = userService.register(user);

      if (!registered) {
        response.put(SUCCESS_KEY, false);
        response.put(MESSAGE_KEY, "User already exists!");
        result = ResponseEntity.status(HttpStatus.CONFLICT).body(response);
      } else {
        response.put(SUCCESS_KEY, true);
        response.put(MESSAGE_KEY, "User registered successfully");
        result = ResponseEntity.ok(response);
      }

    } catch (final Exception e) {
      response.put(SUCCESS_KEY, false);
      response.put(MESSAGE_KEY, "Registration failed: " + e.getMessage());
      result = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    return result;
  }

  /**
   * Handles user login and JWT token generation.
   */
  @PostMapping("/auth/login")
  public ResponseEntity<Map<String, Object>> login(final @RequestBody AppUser request) {
    final Map<String, Object> response = new HashMap<>();
    ResponseEntity<Map<String, Object>> result;

    try {
      final Authentication authentication = authManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                      request.getUsername(), request.getPassword())
      );

      final UserDetails userDetails = (UserDetails) authentication.getPrincipal();

      final AppUser dummyUser = new AppUser();
      dummyUser.setUsername(userDetails.getUsername());
      dummyUser.setRole(userDetails.getAuthorities().iterator().next().getAuthority());

      final String token = jwtUtil.generateToken(dummyUser);

      response.put(SUCCESS_KEY, true);
      response.put(MESSAGE_KEY, "Login successful");
      response.put("token", token);
      response.put("username", userDetails.getUsername());
      response.put("role", dummyUser.getRole());

      result = ResponseEntity.ok(response);

    } catch (final Exception e) {
      response.put(SUCCESS_KEY, false);
      response.put(MESSAGE_KEY, "Invalid username or password");
      result = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    return result;
  }

  /**
   * Retrieves all blogs for authenticated users.
   */
  @GetMapping("/blogs")
  public ResponseEntity<Map<String, Object>> getAllBlogs(final Authentication authentication) {
    final Map<String, Object> response = new HashMap<>();
    ResponseEntity<Map<String, Object>> result;

    if (!isAuthenticated(authentication)) {
      response.put(SUCCESS_KEY, false);
      response.put(MESSAGE_KEY, AUTH_REQUIRED);
      result = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    } else {
      try {
        final List<Blog> blogs = blogRepository.findAll();
        response.put(SUCCESS_KEY, true);
        response.put(DATA_KEY, blogs);
        response.put("count", blogs.size());
        result = ResponseEntity.ok(response);

      } catch (final Exception e) {
        response.put(SUCCESS_KEY, false);
        response.put(MESSAGE_KEY, "Failed to fetch blogs: " + e.getMessage());
        result = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
      }
    }

    return result;
  }

  /**
   * Retrieves a specific blog by its ID.
   */
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

  /**
   * Creates a new blog post.
   */
  @PostMapping("/blogs")
  public ResponseEntity<Map<String, Object>> createBlog(@RequestBody Blog blog, Authentication authentication) {
    Map<String, Object> response = new HashMap<>();

    if (!isAuthenticated(authentication)) {
      response.put("success", false);
      response.put("message", "Authentication required");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    try {
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

  /**
   * Updates an existing blog post.
   */
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

      blog.setId(id);
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

  /**
   * Handles user logout.
   */
  @PostMapping("/auth/logout")
  public ResponseEntity<Map<String, Object>> logout(
          final HttpServletRequest request,
          final HttpServletResponse response) {
    final Map<String, Object> responseMap = new HashMap<>();
    ResponseEntity<Map<String, Object>> result;

    try {
      final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null) {
        logoutHandler.logout(request, response, auth);
      }
      responseMap.put(SUCCESS_KEY, true);
      responseMap.put(MESSAGE_KEY, "Logged out successfully");
      result = ResponseEntity.ok(responseMap);

    } catch (final Exception e) {
      responseMap.put(SUCCESS_KEY, false);
      responseMap.put(MESSAGE_KEY, "Logout failed: " + e.getMessage());
      result = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
    }

    return result;
  }

  /**
   * Deletes a blog post by ID.
   */
  @DeleteMapping("/blogs/{blogId}")
  public ResponseEntity<Map<String, Object>> deleteBlog(
          final @PathVariable("blogId") Long blogId,
          final Authentication authentication) {
    final Map<String, Object> response = new HashMap<>();
    ResponseEntity<Map<String, Object>> result;

    if (!isAuthenticated(authentication)) {
      response.put(SUCCESS_KEY, false);
      response.put(MESSAGE_KEY, AUTH_REQUIRED);
      result = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    } else {
      try {
        final Optional<Blog> existingBlog = blogRepository.findById(blogId);

        if (!existingBlog.isPresent()) {
          response.put(SUCCESS_KEY, false);
          response.put(MESSAGE_KEY, NOT_FOUND);
          result = ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
          blogRepository.deleteById(blogId);

          response.put(SUCCESS_KEY, true);
          response.put(MESSAGE_KEY, "Blog deleted successfully");
          result = ResponseEntity.ok(response);
        }

      } catch (final Exception e) {
        response.put(SUCCESS_KEY, false);
        response.put(MESSAGE_KEY, "Failed to delete blog: " + e.getMessage());
        result = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
      }
    }

    return result;
  }

  /**
   * Health check endpoint.
   */
  @GetMapping("/health")
  public ResponseEntity<Map<String, String>> healthCheck() {
    final Map<String, String> response = new HashMap<>();
    response.put("status", "UP");
    response.put(MESSAGE_KEY, "Blog API is running");
    return ResponseEntity.ok(response);
  }

  /**
   * Helper method to check if user is authenticated.
   */
  private boolean isAuthenticated(final Authentication authentication) {
    return authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken);
  }

  /**
   * Helper method to validate blog input.
   */
  /**
   * Helper method to validate blog input.
   */
  private String validateBlogInput(final Blog blog) {
    if (blog.getTitle() == null || blog.getTitle().trim().isEmpty()) {
      return TITLE_REQUIRED; // Use existing constant
    }

    if (blog.getContent() == null || blog.getContent().trim().isEmpty()) {
      return CONTENT_REQUIRED; // Use existing constant
    }

    return null;
  }
}