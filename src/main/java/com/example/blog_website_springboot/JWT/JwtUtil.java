package com.example.blog_website_springboot.JWT;

import com.example.blog_website_springboot.Model.AppUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.security.Key;

@Component
public class JwtUtil {

    private final String SECRET_KEY = "my_super_secret_key_which_is_very_long123"; // At least 256-bit key required for HS256
    private final long EXPIRATION_TIME = 1000 * 60; // 1 minute in milliseconds

    protected Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // Generate JWT using AppUser (with role)
    public String generateToken(AppUser user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Generate JWT using just username
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extract username (subject) from JWT
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Validate the JWT (signature and expiration)
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);

            // Explicitly check expiration
            return !claims.getBody().getExpiration().before(new Date());

        } catch (ExpiredJwtException e) {
            System.out.println(" JWT expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println(" Unsupported JWT: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println(" Malformed JWT: " + e.getMessage());
        } catch (SignatureException e) {
            System.out.println(" Invalid signature: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println(" Illegal argument: " + e.getMessage());
        }

        return false;
    }
}
