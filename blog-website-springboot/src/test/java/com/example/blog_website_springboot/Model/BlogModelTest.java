package com.example.blog_website_springboot.Model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlogModelTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        Blog blog = new Blog();
        blog.setId(1L);
        blog.setTitle("Test Title");
        blog.setContent("Test Content");
        blog.setUsername("testuser");

        assertEquals(1L, blog.getId());
        assertEquals("Test Title", blog.getTitle());
        assertEquals("Test Content", blog.getContent());
        assertEquals("testuser", blog.getUsername());
    }

    @Test
    void testAllArgsConstructor() {
        Blog blog = new Blog(2L, "Title2", "Content2", "user2");

        assertEquals(2L, blog.getId());
        assertEquals("Title2", blog.getTitle());
        assertEquals("Content2", blog.getContent());
        assertEquals("user2", blog.getUsername());
    }

    @Test
    void testConstructorWithIdTitleContent() {
        Blog blog = new Blog(3L, "Title3", "Content3");

        assertEquals(3L, blog.getId());
        assertEquals("Title3", blog.getTitle());
        assertEquals("Content3", blog.getContent());
        assertNull(blog.getUsername());
    }

    @Test
    void testConstructorWithTitleContentUsername() {
        Blog blog = new Blog("Title4", "Content4", "user4");

        assertNull(blog.getId());
        assertEquals("Title4", blog.getTitle());
        assertEquals("Content4", blog.getContent());
        assertEquals("user4", blog.getUsername());
    }
}
