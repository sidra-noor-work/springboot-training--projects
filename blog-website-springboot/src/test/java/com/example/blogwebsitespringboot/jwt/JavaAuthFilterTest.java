package com.example.blogwebsitespringboot.jwt;
import com.example.blogwebsitespringboot.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class JavaAuthFilterTest {

    private JwtUtil jwtUtil;
    private UserService userService;
    private JwtAuthFilter jwtAuthFilter;
    private FilterChain filterChain;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    public void setup() {
        jwtUtil = mock(JwtUtil.class);
        userService = mock(UserService.class);
        jwtAuthFilter = new JwtAuthFilter(jwtUtil, userService);
        filterChain = mock(FilterChain.class);
        response = new MockHttpServletResponse();
        request = new MockHttpServletRequest();
        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldNotFilter_signupPath() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/auth/signup");
        assertTrue(jwtAuthFilter.shouldNotFilter(req));
    }

    @Test
    public void shouldNotFilter_loginPath() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/auth/login");
        assertTrue(jwtAuthFilter.shouldNotFilter(req));
    }

    @Test
    public void shouldNotFilter_h2ConsolePath() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/h2-console/test");
        assertTrue(jwtAuthFilter.shouldNotFilter(req));
    }

    @Test
    public void shouldFilter_otherPath() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/blogs");
        assertFalse(jwtAuthFilter.shouldNotFilter(req));
    }

    @Test
    public void testDoFilterInternal_validJwtInHeader() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer valid.jwt.token");
        req.setRequestURI("/blogs");

        User userDetails = new User("testuser", "pass", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        when(jwtUtil.extractUsername("valid.jwt.token")).thenReturn("testuser");
        when(jwtUtil.validateToken("valid.jwt.token")).thenReturn(true);
        when(userService.loadUserByUsername("testuser")).thenReturn(userDetails);

        jwtAuthFilter.doFilterInternal(req, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("testuser", auth.getName());
        verify(filterChain).doFilter(req, response);
    }

    @Test
    public void testDoFilterInternal_validJwtInCookie() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie("jwt", "cookie.jwt.token"));
        req.setRequestURI("/blogs");

        User userDetails = new User("cookieUser", "pass", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        when(jwtUtil.extractUsername("cookie.jwt.token")).thenReturn("cookieUser");
        when(jwtUtil.validateToken("cookie.jwt.token")).thenReturn(true);
        when(userService.loadUserByUsername("cookieUser")).thenReturn(userDetails);

        jwtAuthFilter.doFilterInternal(req, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("cookieUser", auth.getName());
        verify(filterChain).doFilter(req, response);
    }

    @Test
    public void testDoFilterInternal_invalidJwt() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer invalid.token");
        req.setRequestURI("/blogs");

        when(jwtUtil.extractUsername("invalid.token")).thenReturn("someone");
        when(jwtUtil.validateToken("invalid.token")).thenReturn(false);

        jwtAuthFilter.doFilterInternal(req, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(req, response);
    }

    @Test
    public void testDoFilterInternal_jwtExtractionFails() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer broken.token");
        req.setRequestURI("/blogs");

        when(jwtUtil.extractUsername("broken.token")).thenThrow(new RuntimeException("Parsing error"));

        jwtAuthFilter.doFilterInternal(req, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(req, response);
    }

    @Test
    public void testDoFilterInternal_noJwt() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/blogs");

        jwtAuthFilter.doFilterInternal(req, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(req, response);
    }


    ///
    @Test
    public void shouldNotFilter_cssPath() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/css/style.css");
        assertTrue(jwtAuthFilter.shouldNotFilter(req));
    }

    @Test
    public void shouldNotFilter_jsPath() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/js/app.js");
        assertTrue(jwtAuthFilter.shouldNotFilter(req));
    }

    @Test
    public void shouldNotFilter_oauth2Path() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/oauth2/callback");
        assertTrue(jwtAuthFilter.shouldNotFilter(req));
    }

    @Test
    public void testDoFilterInternal_authenticationAlreadySet() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer valid.jwt.token");
        req.setRequestURI("/blogs");

        // Set Authentication already present in SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(mock(Authentication.class));

        jwtAuthFilter.doFilterInternal(req, response, filterChain);

        // Should skip auth setup, but continue filter chain
        verify(filterChain).doFilter(req, response);
    }

    @Test
    public void testDoFilterInternal_jwtNull_noAuthSetup() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/blogs");

        // Simulate cookies without jwt cookie
        req.setCookies(new Cookie("other", "value"));

        jwtAuthFilter.doFilterInternal(req, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(req, response);
    }

    @Test
    public void testDoFilterInternal_jwtInvalidTokenThrowsException() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer bad.token");
        req.setRequestURI("/blogs");

        when(jwtUtil.extractUsername("bad.token")).thenThrow(new RuntimeException("Bad token"));

        jwtAuthFilter.doFilterInternal(req, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(req, response);
    }

}
