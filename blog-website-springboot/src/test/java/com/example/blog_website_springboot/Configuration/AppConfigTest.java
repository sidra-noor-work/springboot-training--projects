package com.example.blog_website_springboot.Configuration;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    @Test
    void passwordEncoderBeanShouldBeCreated() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        PasswordEncoder passwordEncoder = context.getBean(PasswordEncoder.class);

        assertNotNull(passwordEncoder);
        String rawPassword = "mySecret";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));

        context.close();
    }
}
