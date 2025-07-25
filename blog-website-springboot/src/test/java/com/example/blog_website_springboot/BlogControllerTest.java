package com.example.blog_website_springboot;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class BlogControllerTest {

    private BlogRepository blogRepository = mock(BlogRepository.class);
    private UserService userService = mock(UserService.class);
    private AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private JwtUtil jwtUtil = mock(JwtUtil.class);
    private HttpServletResponse response = mock(HttpServletResponse.class);

    private BlogController blogController;
    // Create mock
    UserDetails userDetails = mock(UserDetails.class);

    // Create authority list
    GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
    List<GrantedAuthority> authorities = Collections.singletonList(authority);


    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        blogController = new BlogController(blogRepository);
        injectField(blogController, "userService", userService);
        injectField(blogController, "authenticationManager", authenticationManager);
        injectField(blogController, "jwtUtil", jwtUtil);
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
    void testLoginSuccess() {
        AppUser request = new AppUser();
        request.setUsername("john");
        request.setPassword("password");

        // Setup authorities list
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        // Use real User object instead of mocking UserDetails
        User userDetails = new User("john", "password", authorities);

        Authentication authentication = mock(Authentication.class);
        Model model = mock(Model.class);
        response = new MockHttpServletResponse();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(any(AppUser.class))).thenReturn("dummy.jwt.token");

        String result = blogController.login(request, response, model);
        assertEquals("redirect:/create", result);
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
    void testLoginPage() {
        assertEquals("login", blogController.loginPage());
    }

    @Test
    void testSignupPage() {
        Model model = mock(Model.class);
        String result = blogController.signupForm(model);
        assertEquals("signup", result);
    }

    @Test
    void testHomeRedirect() {
        assertEquals("redirect:/login", blogController.homeRedirect());
    }
}
