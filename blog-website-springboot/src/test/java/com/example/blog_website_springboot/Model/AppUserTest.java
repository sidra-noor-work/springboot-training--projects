package com.example.blog_website_springboot.Model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppUserTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("testpassword");
        user.setRole("ROLE_USER");

        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("testpassword", user.getPassword());
        assertEquals("ROLE_USER", user.getRole());
    }

    @Test
    void testAllArgsConstructor() {
        AppUser user = new AppUser("user1", "pass1");

        assertNull(user.getId()); // id is null by default
        assertEquals("user1", user.getUsername());
        assertEquals("pass1", user.getPassword());
        assertNull(user.getRole()); // role is null
    }

    @Test
    void testCustomSettersAndGetters() {
        AppUser user = new AppUser();
        user.setId(5L);
        user.setUsername("anotherUser");
        user.setPassword("anotherPass");
        user.setRole("ADMIN");

        assertEquals(5L, user.getId());
        assertEquals("anotherUser", user.getUsername());
        assertEquals("anotherPass", user.getPassword());
        assertEquals("ADMIN", user.getRole());
    }
}
