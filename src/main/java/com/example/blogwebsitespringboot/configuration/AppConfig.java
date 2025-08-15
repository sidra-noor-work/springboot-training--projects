package com.example.blogwebsitespringboot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Application-wide configuration for security-related beans.
 */
@SuppressWarnings("PMD.AtLeastOneConstructor")
@Configuration
public class AppConfig {

    /**
     * Creates a BCrypt password encoder bean.
     *
     * @return the password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
