package com.example.blogwebsitespringboot.controller;

import com.example.blogwebsitespringboot.jwt.JwtUtil;
import com.example.blogwebsitespringboot.repository.BlogRepository;
import com.example.blogwebsitespringboot.service.BlogService;
import com.example.blogwebsitespringboot.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BlogControllerTest {

    @Mock
    private BlogService blogService;

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private SecurityContextLogoutHandler logoutHandler;

    @InjectMocks
    private BlogController blogController;

    private AutoCloseable closeable;

    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
        SecurityContextHolder.clearContext();
    }

    @Test
    void testLogout_Success() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication auth = mock(Authentication.class);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        doNothing().when(logoutHandler).logout(request, response, auth);

        ResponseEntity<Map<String, Object>> result = blogController.logout(request, response);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue((Boolean) result.getBody().get("success"));
        assertEquals("Logged out successfully", result.getBody().get("message"));

        verify(logoutHandler, times(1)).logout(request, response, auth);
    }

    @Test
    void testLogout_NoAuthentication() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        // No auth set intentionally
        SecurityContextHolder.setContext(securityContext);

        ResponseEntity<Map<String, Object>> result = blogController.logout(request, response);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue((Boolean) result.getBody().get("success"));
        assertEquals("Logged out successfully", result.getBody().get("message"));

        // logoutHandler.logout should never be called since auth is null
        verify(logoutHandler, never()).logout(any(), any(), any());
    }

    @Test
    void testLogout_Failure() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication auth = mock(Authentication.class);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        doThrow(new RuntimeException("Logout error")).when(logoutHandler).logout(request, response, auth);

        ResponseEntity<Map<String, Object>> result = blogController.logout(request, response);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertFalse((Boolean) result.getBody().get("success"));
        assertTrue(result.getBody().get("message").toString().contains("Logout failed"));

        verify(logoutHandler, times(1)).logout(request, response, auth);
    }
    @Test
    void testLogout_whenAuthIsNull() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);

        // Mock SecurityContext to return null authentication
        SecurityContextHolder.getContext().setAuthentication(null);

        ResponseEntity<Map<String, Object>> response = blogController.logout(req, res);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Logged out successfully", response.getBody().get("message"));
    }

    @Test
    void testLogout_whenLogoutHandlerThrowsException() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);

        Authentication auth = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(auth);

        doThrow(new RuntimeException("Logout error")).when(logoutHandler).logout(req, res, auth);

        ResponseEntity<Map<String, Object>> response = blogController.logout(req, res);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertTrue(response.getBody().get("message").toString().contains("Logout failed"));
    }

}
