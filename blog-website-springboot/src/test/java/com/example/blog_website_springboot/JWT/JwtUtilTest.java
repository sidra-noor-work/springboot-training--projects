package com.example.blog_website_springboot.JWT;


import com.example.blog_website_springboot.Model.AppUser;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String secretKey = "my_super_secret_key_which_is_very_long123"; // 32+ chars

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(secretKey);
    }

    @Test
    void testGenerateToken_validUser_returnsToken() {
        AppUser user = new AppUser();
        user.setUsername("john");
        user.setRole("ROLE_USER");

        String token = jwtUtil.generateToken(user);
        assertNotNull(token);
        assertTrue(token.startsWith("ey")); // JWT tokens usually start like this
    }


    @Test
    void testGenerateToken_userWithoutUsername_throwsException() {
        AppUser user = new AppUser();
        user.setUsername(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            jwtUtil.generateToken(user);
        });
        assertEquals("Invalid user provided to generateToken", exception.getMessage());
    }

    @Test
    void testValidateToken_validToken_returnsTrue() {
        AppUser user = new AppUser();
        user.setUsername("alice");
        user.setRole("ROLE_USER");

        String token = jwtUtil.generateToken(user);
        boolean isValid = jwtUtil.validateToken(token);
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_expiredToken_returnsFalse() {
        AppUser user = new AppUser();
        user.setUsername("bob");
        user.setRole("ROLE_USER");

        // Manually create an expired token
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
        String expiredToken = io.jsonwebtoken.Jwts.builder()
                .setSubject("bob")
                .claim("role", "ROLE_USER")
                .setIssuedAt(new Date(System.currentTimeMillis() - 10000)) // issued in the past
                .setExpiration(new Date(System.currentTimeMillis() - 5000)) // already expired
                .signWith(key)
                .compact();

        assertFalse(jwtUtil.validateToken(expiredToken));
    }

    @Test
    void testValidateToken_malformedToken_returnsFalse() {
        assertFalse(jwtUtil.validateToken("this.is.not.a.jwt"));
    }

    @Test
    void testValidateToken_nullToken_returnsFalse() {
        assertFalse(jwtUtil.validateToken(null));
    }

    @Test
    void testValidateToken_emptyToken_returnsFalse() {
        assertFalse(jwtUtil.validateToken(""));
    }
}
