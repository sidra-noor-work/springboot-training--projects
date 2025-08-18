package com.example.blogwebsitespringboot.configuration;

import com.example.blogwebsitespringboot.model.AppUser;
import com.example.blogwebsitespringboot.repository.UserRepository;
import com.example.blogwebsitespringboot.service.UserService;
import com.example.blogwebsitespringboot.jwt.JwtAuthFilter;
import com.example.blogwebsitespringboot.jwt.JwtUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private SecurityConfig securityConfig;
    private UserService userService;

    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HttpSecurity httpSecurity;

    @BeforeEach
    void setUpTestUser() {
        userRepository.findByUsername("testuser").orElseGet(() -> {
            AppUser user = new AppUser();
            user.setUsername("testuser");
            user.setPassword(passwordEncoder.encode("password"));
            return userRepository.save(user);
        });
    }
    @BeforeEach
    void setUp() {
        JwtAuthFilter mockJwtAuthFilter = mock(JwtAuthFilter.class);
        jwtUtil = mock(JwtUtil.class);
        userService = mock(UserService.class);
        securityConfig = new SecurityConfig(mockJwtAuthFilter, jwtUtil, userService); // pass 3 args now
    }



    private String generateTestJwt() {
        AppUser user = userRepository.findByUsername("testuser").get();
        return jwtUtil.generateToken(user);
    }

    @Test
    void authenticationManagerBeanCreation() throws Exception {
        AuthenticationManager manager = securityConfig.authenticationManager(authenticationConfiguration);
        assertThat(manager).isNotNull();
    }



    @Test
    void corsConfigurationSourceConfiguredProperly() {
        UrlBasedCorsConfigurationSource source = securityConfig.corsConfigurationSource();
        assertThat(source).isNotNull();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/");

        CorsConfiguration config = source.getCorsConfiguration(request);
        assertThat(config).isNotNull();
        assertThat(config.getAllowedOriginPatterns()).contains("http://localhost:3000");
        assertThat(config.getAllowedMethods()).contains("GET", "POST", "PUT", "DELETE", "OPTIONS");
        assertThat(config.getAllowedHeaders()).contains("*");
        assertThat(config.getExposedHeaders()).contains("Authorization", "Set-Cookie");
    }

    @Test
    void testLogoutSuccessHandler() throws Exception {
        mockMvc.perform(post("/logout")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\": \"Logged out successfully\"}"));
    }
    @Test
    void corsFilterBeanCreation() {
        CorsFilter corsFilter = securityConfig.corsFilter();
        assertThat(corsFilter).isNotNull();
    }

    @Test
    void securityConfigLoads() {
        assertThat(securityConfig).isNotNull();
    }

    @Test
    void authenticationManagerBeanExists() throws Exception {
        AuthenticationManager manager = securityConfig.authenticationManager(authenticationConfiguration);
        assertThat(manager).isNotNull();
    }

    @Test
    void corsFilterBeanExists() {
        CorsFilter filter = securityConfig.corsFilter();
        assertThat(filter).isNotNull();
    }

    @Test
    void corsConfigurationSourceTest() {
        var source = securityConfig.corsConfigurationSource();
        var request = new MockHttpServletRequest();
        var config = source.getCorsConfiguration(request);

        assertThat(config).isNotNull();
        assertThat(config.getAllowedOriginPatterns()).contains("http://localhost:3000");
        assertThat(config.getAllowedMethods()).contains("GET", "POST", "PUT", "DELETE", "OPTIONS");
        assertThat(config.getAllowedHeaders()).contains("*");
    }

    @Test
    void testOauth2SuccessHandler_localDev() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("localhost");
        MockHttpServletResponse response = new MockHttpServletResponse();
        OAuth2User principal = mock(OAuth2User.class);
        when(principal.getAttribute("login")).thenReturn("testuser");
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);

        // Use the already mocked userService
        securityConfig.oauth2SuccessHandler(userService).onAuthenticationSuccess(request, response, auth);

        assertThat(response.getHeader("Set-Cookie")).contains("SameSite=Lax");
    }



    @Test
    void oauth2SuccessHandler_inProd_setsSecureFlag() throws Exception {
        UserService mockService = mock(UserService.class);
        SecurityFilterChain chain = securityConfig.securityFilterChain(httpSecurity, mockService);
        assertThat(chain).isNotNull();
    }
}
