package com.example.blogwebsitespringboot.controller;

import com.example.blogwebsitespringboot.jwt.JwtUtil;
import com.example.blogwebsitespringboot.model.AppUser;
import com.example.blogwebsitespringboot.model.Blog;
import com.example.blogwebsitespringboot.repository.BlogRepository;
import com.example.blogwebsitespringboot.service.BlogService;
import com.example.blogwebsitespringboot.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;

class BlogTest {
    @Autowired
    private BlogRepository blogRepository;
    @Autowired
    private SecurityContextLogoutHandler mockLogoutHandler;
    private UserService userService = mock(UserService.class);
    private AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private JwtUtil jwtUtil = mock(JwtUtil.class);
    private BlogService blogService;


    private AppUser mockUser;
    private Blog mockBlog;
    private List<GrantedAuthority> authorities;
    @InjectMocks
    private BlogController blogController;

    UserDetails userDetails = new User(
            "testuser",
            "password",
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
    );

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        // Mock dependencies
        blogRepository = mock(BlogRepository.class);
        blogService = new BlogService(blogRepository); // If BlogService is simple enough
        userService = mock(UserService.class);
        authenticationManager = mock(AuthenticationManager.class);
        // JwtUtil is static utility, no need to instantiate
        // jwtUtil = new JwtUtil("my_super_secret_key_which_is_very_long123456"); // Remove this line

        // Instantiate controller with all required constructor arguments
        blogController = new BlogController(
                blogService,
                blogRepository,
                userService,
                authenticationManager,
                jwtUtil,
                mockLogoutHandler
        );

        // Mock user
        mockUser = new AppUser();
        mockUser.setUsername("testuser");
        mockUser.setPassword("password");
        mockUser.setRole("USER");

        // Mock blog
        mockBlog = new Blog(1L, "Test Title", "Test Content");
        mockBlog.setUsername("testuser");

        // Setup authority
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        authorities = Collections.singletonList(authority);
    }

    private Authentication mockAuth() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        return auth;
    }
    @Test
    void testUpdateBlog_EmptyContent() {
        Blog blog = new Blog();
        blog.setTitle("Valid Title");
        blog.setContent(""); // Empty
        Authentication auth = createMockAuthentication();

        when(blogRepository.findById(1L)).thenReturn(Optional.of(mockBlog));

        ResponseEntity<Map<String, Object>> response = blogController.updateBlog(1L, blog, auth);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Blog content is required", response.getBody().get("message"));
    }
    @Test
    void testDeleteBlog_Exception() {
        when(blogRepository.findById(1L)).thenReturn(Optional.of(mockBlog));
        doThrow(new RuntimeException("Delete failed")).when(blogRepository).deleteById(1L);
        Authentication auth = createMockAuthentication();

        ResponseEntity<Map<String, Object>> response = blogController.deleteBlog(1L, auth);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to delete blog: Delete failed", response.getBody().get("message"));
    }

    @Test
    void testCreateBlog_AuthNotAuthenticated() {
        Blog blog = new Blog();
        blog.setTitle("Title");
        blog.setContent("Content");

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = blogController.createBlog(blog, auth);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
    }


    @Test
    void testIsAuthenticated_Anonymous() {
        Authentication auth = mock(AnonymousAuthenticationToken.class);
        when(auth.isAuthenticated()).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = blogController.getAllBlogs(auth);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
    @Test
    void testSignup_EmptyUsername() {
        AppUser user = new AppUser();
        user.setUsername("");
        user.setPassword("somepassword");

        ResponseEntity<Map<String, Object>> response = blogController.signup(user);
        // Assuming current controller returns CONFLICT for empty user (same as user exists)
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void testLogin_EmptyUsernameOrPassword() {
        AppUser user = new AppUser();
        user.setUsername("");
        user.setPassword("");

        ResponseEntity<Map<String, Object>> response = blogController.login(user);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }




    @Test
    void testUpdateBlog_Exception() {
        Blog blog = new Blog(1L, "Title", "Content");
        blog.setUsername("testuser");
        Authentication auth = createMockAuthentication();

        when(blogRepository.findById(1L)).thenReturn(Optional.of(mockBlog));
        when(blogRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<Map<String, Object>> response = blogController.updateBlog(1L, blog, auth);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to update blog: DB error", response.getBody().get("message"));
    }

    private void injectField(Object target, String fieldName, Object toInject) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, toInject);
    }

    // JWT Utility Tests
    @Test
    void testGenerateTokenWithUser() {
        String token = JwtUtil.generateToken(mockUser);
        assertNotNull(token, "Token should not be null");
        assertTrue(token.length() > 0, "Token should not be empty");
    }

    @Test
    void testGenerateTokenWithUsername() {
        String token = JwtUtil.generateToken("testuser");
        assertNotNull(token, "Token should not be null");
    }

    @Test
    void testExtractUsername() {
        String token = JwtUtil.generateToken("testuser");
        String username = JwtUtil.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void testValidateToken_validToken() {
        String token = JwtUtil.generateToken("testuser");
        assertTrue(JwtUtil.validateToken(token));
    }

    // Authentication API Tests


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
    void testSignupUserAlreadyExists() {
        AppUser user = new AppUser();
        user.setUsername("existinguser");
        user.setPassword("123");

        when(userService.register(any(AppUser.class))).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = blogController.signup(user);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
    }

    @Test
    void testCreateBlogMissingTitle() {
        Blog blog = new Blog();
        blog.setContent("Some content");

        ResponseEntity<Map<String, Object>> response = blogController.createBlog(blog, mockAuth());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    @Test
    void testLoginFailure() {
        AppUser user = new AppUser();
        user.setUsername("wrong");
        user.setPassword("bad");

        when(authenticationManager.authenticate(any())).thenThrow(new RuntimeException("Bad creds"));

        ResponseEntity<Map<String, Object>> response = blogController.login(user);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
    }

    @Test
    void testSignupException() {
        AppUser user = new AppUser();
        user.setUsername("test");
        user.setPassword("123");

        when(userService.register(any())).thenThrow(new RuntimeException("DB down"));

        ResponseEntity<Map<String, Object>> response = blogController.signup(user);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
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
    @Test
    void testUpdateBlog_NotFound() {
        when(blogRepository.findById(99L)).thenReturn(Optional.empty());
        Authentication auth = createMockAuthentication();

        ResponseEntity<Map<String, Object>> response = blogController.updateBlog(99L, mockBlog, auth);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Blog not found", response.getBody().get("message"));
    }

    @Test
    void testSignup_EmptyUsernameOrPassword() {
        AppUser user = new AppUser();
        user.setUsername("");
        user.setPassword("");

        ResponseEntity<Map<String, Object>> response = blogController.signup(user);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode()); // or BAD_REQUEST depending on your logic

        // Or, if you want to extend controller to validate, you can assert bad request response
    }

    @Test
    void testCreateBlog_AuthNull() {
        Blog blog = new Blog();
        blog.setTitle("Hello");
        blog.setContent("World");

        ResponseEntity<Map<String, Object>> response = blogController.createBlog(blog, null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
    @Test
    void testCreateBlog_EmptyContent() {
        Blog blog = new Blog();
        blog.setTitle("Title");
        blog.setContent(""); // Empty
        Authentication auth = createMockAuthentication();

        ResponseEntity<Map<String, Object>> response = blogController.createBlog(blog, auth);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Blog content is required", response.getBody().get("message"));
    }
    @Test
    void testIsAuthenticated_NullAuth() {
        ResponseEntity<Map<String, Object>> response = blogController.getAllBlogs(null);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testIsAuthenticated_NotAuthenticated() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = blogController.getAllBlogs(auth);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testIsAuthenticated_AnonymousAuth() {
        Authentication auth = mock(AnonymousAuthenticationToken.class);
        when(auth.isAuthenticated()).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = blogController.getAllBlogs(auth);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testIsAuthenticated_ValidAuth() {
        Authentication auth = createMockAuthentication(); // Your existing helper
        when(blogRepository.findAll()).thenReturn(List.of());

        ResponseEntity<Map<String, Object>> response = blogController.getAllBlogs(auth);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    @Test
    void testCreateBlog_TitleWhitespace() {
        Blog blog = new Blog();
        blog.setTitle("   ");
        blog.setContent("Valid content");
        Authentication auth = createMockAuthentication();

        ResponseEntity<Map<String, Object>> response = blogController.createBlog(blog, auth);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Blog title is required", response.getBody().get("message"));
    }

    @Test
    void testUpdateBlog_ContentWhitespace() {
        Blog blog = new Blog();
        blog.setTitle("Valid title");
        blog.setContent("  ");
        Authentication auth = createMockAuthentication();
        when(blogRepository.findById(1L)).thenReturn(Optional.of(mockBlog));

        ResponseEntity<Map<String, Object>> response = blogController.updateBlog(1L, blog, auth);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Blog content is required", response.getBody().get("message"));
    }





    // Helper method to create mock authentication
    private Authentication createMockAuthentication() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("testuser");
        return auth;
    }
}