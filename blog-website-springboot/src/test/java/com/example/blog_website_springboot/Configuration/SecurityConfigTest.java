package com.example.blog_website_springboot.Configuration;

import com.example.blog_website_springboot.JWT.JwtAuthFilter;
import com.example.blog_website_springboot.JWT.JwtUtil;
import com.example.blog_website_springboot.Service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.web.filter.CorsFilter;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest // Full context load
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;

    @Test
    void authenticationManagerBeanCreation() throws Exception {
        AuthenticationManager manager = securityConfig.authenticationManager(authenticationConfiguration);
        assertThat(manager).isNotNull();
    }

    @Test
    void corsFilterBeanCreation() {
        CorsFilter corsFilter = securityConfig.corsFilter();
        assertThat(corsFilter).isNotNull();
    }


    @Test
    void corsConfigurationSourceTest() {
        var source = securityConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/"); // or any URL you want to simulate

        var config = source.getCorsConfiguration(request);

        assertThat(config).isNotNull();
        assertThat(config.getAllowedOriginPatterns()).contains("http://localhost:3000");
        assertThat(config.getAllowedMethods()).contains("GET", "POST", "PUT", "DELETE", "OPTIONS");
        assertThat(config.getAllowedHeaders()).contains("*");
    }

}