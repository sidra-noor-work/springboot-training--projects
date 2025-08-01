package com.example.blog_website_springboot;

import com.example.blog_website_springboot.Controller.BlogController;
import com.example.blog_website_springboot.JWT.JwtUtil;
import com.example.blog_website_springboot.Model.AppUser;
import com.example.blog_website_springboot.Model.Blog;
import com.example.blog_website_springboot.Repository.BlogRepository;
import com.example.blog_website_springboot.Service.BlogService;
import com.example.blog_website_springboot.Service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BlogTest {

    private BlogRepository blogRepository = mock(BlogRepository.class);
    private UserService userService = mock(UserService.class);
    private AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private JwtUtil jwtUtil = mock(JwtUtil.class);
    private BlogService blogService;
    private BlogController blogController;

    private AppUser mockUser;
    private Blog mockBlog;
    private List<GrantedAuthority> authorities;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        blogService = new BlogService(blogRepository);
        blogController = new BlogController(blogRepository);

        // Inject dependencies using reflection
        injectField(blogController, "userService", userService);
        injectField(blogController, "authenticationManager", authenticationManager);
        injectField(blogController, "jwtUtil", jwtUtil);
        injectField(blogController, "blogService", blogService);

        // Create mock data
        mockUser = new AppUser();
        mockUser.setUsername("testuser");
        mockUser.setPassword("password");
        mockUser.setRole("USER");

        mockBlog = new Blog(1L, "Test Title", "Test Content");
        mockBlog.setUsername("testuser");

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        authorities = Collections.singletonList(authority);
    }

    private void injectField(Object target, String fieldName, Object toInject) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, toInject);
    }

    // JWT Utility Tests
    @Test
    void testGenerateTokenWithUser() {
        JwtUtil realJwtUtil = new JwtUtil();
        String token = realJwtUtil.generateToken(mockUser);
        assertNotNull(token, "Token should not be null");
        assertTrue(token.length() > 0, "Token should not be empty");
    }

    @Test
    void testGenerateTokenWithUsername() {
        JwtUtil realJwtUtil = new JwtUtil();
        String token = realJwtUtil.generateToken("testuser");
        assertNotNull(token, "Token should not be null");
    }

    @Test
    void testExtractUsername() {
        JwtUtil realJwtUtil = new JwtUtil();
        String token = realJwtUtil.generateToken("testuser");
        String username = realJwtUtil.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void testValidateToken_validToken() {
        JwtUtil realJwtUtil = new JwtUtil();
        String token = realJwtUtil.generateToken("testuser");
        assertTrue(realJwtUtil.validateToken(token));
    }

    // Authentication API Tests
    @Test
    void testSignupSuccess() {
        when(userService.register(any(AppUser.class))).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = blogController.signup(mockUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(true, body.get("success"));
        assertEquals("User registered successfully", body.get("message"));
        verify(userService).register(any(AppUser.class));
    }

    @Test
    void testSignupFail_UserExists() {
        when(userService.register(any(AppUser.class))).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = blogController.signup(mockUser);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("User already exists!", body.get("message"));
    }

    @Test
    void testLoginSuccess() {
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = new User("testuser", "password", authorities);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(any(AppUser.class))).thenReturn("dummy.jwt.token");

        ResponseEntity<Map<String, Object>> response = blogController.login(mockUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(true, body.get("success"));
        assertEquals("Login successful", body.get("message"));
        assertEquals("dummy.jwt.token", body.get("token"));
        assertEquals("testuser", body.get("username"));
    }

    @Test
    void testLoginFail_InvalidCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new RuntimeException("Bad credentials"));

        ResponseEntity<Map<String, Object>> response = blogController.login(mockUser);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("Invalid username or password", body.get("message"));
    }

    // Blog API Tests with Mock Authentication
    @Test
    void testGetAllBlogs_WithAuth() {
        List<Blog> mockBlogs = Arrays.asList(
                new Blog(1L, "Title1", "Content1"),
                new Blog(2L, "Title2", "Content2")
        );
        mockBlogs.get(0).setUsername("testuser");
        mockBlogs.get(1).setUsername("testuser");
        mockBlogs.get(0).setUsername("testuser");
        mockBlogs.get(1).setUsername("testuser");

        when(blogRepository.findAll()).thenReturn(mockBlogs);
        Authentication auth = createMockAuthentication();

        ResponseEntity<Map<String, Object>> response = blogController.getAllBlogs(auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(true, body.get("success"));
        assertEquals(2, body.get("count"));
        verify(blogRepository).findAll();
    }

    @Test
    void testGetAllBlogs_WithoutAuth() {
        ResponseEntity<Map<String, Object>> response = blogController.getAllBlogs(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("Authentication required", body.get("message"));
    }

    @Test
    void testGetBlogById_Found() {
        when(blogRepository.findById(1L)).thenReturn(Optional.of(mockBlog));
        Authentication auth = createMockAuthentication();

        ResponseEntity<Map<String, Object>> response = blogController.getBlogById(1L, auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(true, body.get("success"));
        verify(blogRepository).findById(1L);
    }

    @Test
    void testGetBlogById_NotFound() {
        when(blogRepository.findById(99L)).thenReturn(Optional.empty());
        Authentication auth = createMockAuthentication();

        ResponseEntity<Map<String, Object>> response = blogController.getBlogById(99L, auth);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("Blog not found", body.get("message"));
    }

    @Test
    void testCreateBlog_Success() {
        Blog newBlog = new Blog();
        newBlog.setTitle("New Title");
        newBlog.setContent("New Content");

        Blog savedBlog = new Blog(3L, "New Title", "New Content");
        savedBlog.setUsername("testuser");

        when(blogRepository.save(any(Blog.class))).thenReturn(savedBlog);
        Authentication auth = createMockAuthentication();

        ResponseEntity<Map<String, Object>> response = blogController.createBlog(newBlog, auth);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(true, body.get("success"));
        assertEquals("Blog created successfully", body.get("message"));
        verify(blogRepository).save(any(Blog.class));
    }

    @Test
    void testCreateBlog_EmptyTitle() {
        Blog invalidBlog = new Blog();
        invalidBlog.setTitle("");
        invalidBlog.setContent("Content");
        Authentication auth = createMockAuthentication();

        ResponseEntity<Map<String, Object>> response = blogController.createBlog(invalidBlog, auth);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("Blog title is required", body.get("message"));
    }

    @Test
    void testUpdateBlog_Success() {
        Blog updatedBlog = new Blog(1L, "Updated Title", "Updated Content");
        updatedBlog.setUsername("testuser");

        when(blogRepository.findById(1L)).thenReturn(Optional.of(mockBlog));
        when(blogRepository.save(any(Blog.class))).thenReturn(updatedBlog);
        Authentication auth = createMockAuthentication();

        ResponseEntity<Map<String, Object>> response = blogController.updateBlog(1L, updatedBlog, auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(true, body.get("success"));
        assertEquals("Blog updated successfully", body.get("message"));
        verify(blogRepository).findById(1L);
        verify(blogRepository).save(any(Blog.class));
    }

    @Test
    void testDeleteBlog_Success() {
        when(blogRepository.findById(1L)).thenReturn(Optional.of(mockBlog));
        Authentication auth = createMockAuthentication();

        ResponseEntity<Map<String, Object>> response = blogController.deleteBlog(1L, auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(true, body.get("success"));
        assertEquals("Blog deleted successfully", body.get("message"));
        verify(blogRepository).findById(1L);
        verify(blogRepository).deleteById(1L);
    }

    @Test
    void testHealthCheck() {
        ResponseEntity<Map<String, String>> response = blogController.healthCheck();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("UP", body.get("status"));
        assertEquals("Blog API is running", body.get("message"));
    }

    // Blog Service Tests
    @Test
    void testBlogService_GetAll() {
        List<Blog> mockBlogs = Arrays.asList(
                new Blog(1L, "Title1", "Content1"),
                new Blog(2L, "Title2", "Content2")
        );

        when(blogRepository.findAll()).thenReturn(mockBlogs);

        List<Blog> result = blogService.getAll();

        assertEquals(2, result.size());
        assertEquals("Title1", result.get(0).getTitle());
        verify(blogRepository).findAll();
    }

    @Test
    void testBlogService_GetById_Exists() {
        when(blogRepository.findById(1L)).thenReturn(Optional.of(mockBlog));

        Blog result = blogService.get(1L);

        assertNotNull(result);
        assertEquals("Test Title", result.getTitle());
        verify(blogRepository).findById(1L);
    }

    @Test
    void testBlogService_GetById_NotFound() {
        when(blogRepository.findById(99L)).thenReturn(Optional.empty());

        Blog result = blogService.get(99L);

        assertNull(result);
        verify(blogRepository).findById(99L);
    }

    @Test
    void testBlogService_Delete() {
        blogService.delete(5L);

        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        verify(blogRepository).deleteById(captor.capture());
        assertEquals(5L, captor.getValue());
    }

    // Helper method to create mock authentication
    private Authentication createMockAuthentication() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("testuser");
        return auth;
    }
}