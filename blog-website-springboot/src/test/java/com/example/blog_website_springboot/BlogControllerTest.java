package com.example.blog_website_springboot;

import com.example.blog_website_springboot.Controller.BlogController;
import com.example.blog_website_springboot.JWT.JwtUtil;
import com.example.blog_website_springboot.Model.AppUser;
import com.example.blog_website_springboot.Model.Blog;
import com.example.blog_website_springboot.Repository.BlogRepository;
import com.example.blog_website_springboot.Service.BlogService;
import com.example.blog_website_springboot.Service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class BlogControllerTest {

    private BlogRepository blogRepository = mock(BlogRepository.class);
    private UserService userService = mock(UserService.class);
    private AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private JwtUtil jwtUtil = mock(JwtUtil.class);
    private HttpServletResponse response = mock(HttpServletResponse.class);

    private BlogController blogController;
    @Autowired
    private MockMvc mockMvc;
    private BlogService blogService;

    private AppUser mockUser;
    UserDetails userDetails = mock(UserDetails.class);

    GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
    List<GrantedAuthority> authorities = Collections.singletonList(authority);

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        blogRepository = mock(BlogRepository.class);
        blogService = new BlogService(blogRepository);
        blogController = new BlogController(blogRepository);
        injectField(blogController, "userService", userService);
        injectField(blogController, "authenticationManager", authenticationManager);
        injectField(blogController, "jwtUtil", jwtUtil);

        // Create mock AppUser
        mockUser = new AppUser();
        mockUser.setUsername("testuser");
        mockUser.setPassword("password");
        mockUser.setRole("USER");
        mockMvc = MockMvcBuilders.standaloneSetup(new BlogController(blogRepository)).build();

    }

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

    @Test
    void testValidateToken_expiredToken() throws InterruptedException {
        JwtUtil shortLivedJwtUtil = new JwtUtil() {
            @Override
            public String generateToken(String username) {
                return Jwts.builder()
                        .setSubject(username)
                        .setIssuedAt(new Date())
                        .setExpiration(new Date(System.currentTimeMillis() + 1000)) // 1 second
                        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                        .compact();
            }
        };

        String token = shortLivedJwtUtil.generateToken("testuser");
        Thread.sleep(1500); // Wait for token to expire

        boolean isValid = shortLivedJwtUtil.validateToken(token);
        assertFalse(isValid, "Token should be expired");
    }

    @Test
    void testValidateToken_malformedToken() {
        JwtUtil realJwtUtil = new JwtUtil();
        String invalidToken = "not.a.valid.token";
        boolean isValid = realJwtUtil.validateToken(invalidToken);
        assertFalse(isValid, "Malformed token should be invalid");
    }

    @Test
    void testValidateToken_nullToken() {
        JwtUtil realJwtUtil = new JwtUtil();
        boolean isValid = realJwtUtil.validateToken(null);
        assertFalse(isValid, "Null token should be invalid");
    }





    private void injectField(Object target, String fieldName, Object toInject) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, toInject);
    }

    @Test
    void testSignupSuccess() {
        AppUser user = new AppUser();
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        when(userService.register(user)).thenReturn(true);
        String result = blogController.signup(user, redirectAttributes);
        assertEquals("redirect:/login", result);
    }

    @Test
    void testSignupFail_UserExists() {
        AppUser user = new AppUser();
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        when(userService.register(user)).thenReturn(false);
        String result = blogController.signup(user, redirectAttributes);
        assertEquals("redirect:/login", result); // because failure also redirects to login with message
    }

    @Test
    void testLoginFail_InvalidCredentials() {
        AppUser request = new AppUser();
        request.setUsername("wrong");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any())).thenThrow(new RuntimeException("Bad credentials"));

        Model model = mock(Model.class);
        String result = blogController.login(request, response, model);

        assertEquals("login", result);
    }

    @Test
    void testLoginSuccess() {
        AppUser request = new AppUser();
        request.setUsername("john");
        request.setPassword("password");

        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = new User("john", "password", authorities);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(any(AppUser.class))).thenReturn("dummy.jwt.token");

        Model model = mock(Model.class);
        MockHttpServletResponse response = new MockHttpServletResponse();

        String result = blogController.login(request, response, model);
        assertEquals("redirect:/create", result);
    }

    @Test
    void testSignupPage() {
        Model model = mock(Model.class);
        String result = blogController.signupForm(model);
        assertEquals("signup", result);
    }

    @Test
    void testGetAll() {
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
    void testGetByIdExists() {
        Blog blog = new Blog(1L, "Title", "Content");

        when(blogRepository.findById(1L)).thenReturn(Optional.of(blog));

        Blog result = blogService.get(1L);

        assertNotNull(result);
        assertEquals("Title", result.getTitle());
        verify(blogRepository).findById(1L);
    }

    @Test
    void testGetByIdNotFound() {
        when(blogRepository.findById(99L)).thenReturn(Optional.empty());

        Blog result = blogService.get(99L);

        assertNull(result);
        verify(blogRepository).findById(99L);
    }


    @Test
    void testDeleteBlog() {
        blogService.delete(5L);

        // Capture the ID passed to deleteById
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        verify(blogRepository).deleteById(captor.capture());

        assertEquals(5L, captor.getValue());
    }

    @Test
    void testHomeRedirect() {
        assertEquals("redirect:/login", blogController.homeRedirect());
    }
}