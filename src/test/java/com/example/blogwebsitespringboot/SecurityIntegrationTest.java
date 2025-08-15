package com.example.blogwebsitespringboot;

import com.example.blogwebsitespringboot.jwt.JwtUtil;
import com.example.blogwebsitespringboot.model.AppUser;
import com.example.blogwebsitespringboot.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BlogWebsiteSpringbootApplication.class)
@AutoConfigureMockMvc
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setupTestUser() {
        if (userRepository.findByUsername("testuser").isEmpty()) {
            AppUser user = new AppUser();
            user.setUsername("testuser");
            user.setPassword(passwordEncoder.encode("testpassword"));
            user.setRole("USER");
            userRepository.save(user);
        }
    }

    private String generateTestJwt() {
        AppUser user = userRepository.findByUsername("testuser").get();
        return jwtUtil.generateToken(user);
    }



    @Test
    void testProtectedEndpointWithoutJwt() throws Exception {
        mockMvc.perform(get("/blogs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCORSPreflightRequest() throws Exception {
        mockMvc.perform(options("/blogs")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());
    }

    @Test
    void testLogoutHandler() throws Exception {
        mockMvc.perform(post("/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\": \"Logged out successfully\"}"));
    }

    @Test
    void testPublicEndpointAccessible() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newuser\", \"password\":\"newpass\"}"))
                .andExpect(status().isOk());
    }


    @Test
    void testInvalidEndpointShouldFail() throws Exception {
        mockMvc.perform(get("/non-existent-endpoint")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
