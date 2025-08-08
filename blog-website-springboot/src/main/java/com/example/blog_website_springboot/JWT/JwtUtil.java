package com.example.blog_website_springboot.JWT;

import com.example.blog_website_springboot.Model.AppUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.security.Key;
@Component
public class JwtUtil {

    private final String SECRET_KEY;
    private final long EXPIRATION_TIME;

    public JwtUtil() {
        this.SECRET_KEY = "my_super_secret_key_which_is_very_long123";
        this.EXPIRATION_TIME = 1000 * 60 * 60; // 1 hour
    }

    public JwtUtil(String secret) {
        this.SECRET_KEY = secret;
        this.EXPIRATION_TIME = 1000 * 60 * 60;
    }

    protected Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // ✅ Generate JWT from AppUser
    public String generateToken(AppUser user) {
        if (user == null || user.getUsername() == null) {
            throw new IllegalArgumentException("Invalid user provided to generateToken");
        }

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Generate JWT from username only
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);

            return !claims.getBody().getExpiration().before(new Date());

        } catch (ExpiredJwtException | UnsupportedJwtException |
                 MalformedJwtException | SignatureException |
                 IllegalArgumentException e) {
            System.out.println("JWT validation error: " + e.getMessage());
            return false;
        }
    }
}
