package com.example.blogwebsitespringboot.jwt;

import com.example.blogwebsitespringboot.model.AppUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public final class JwtUtil {

    // Secure 256-bit secret key (HS256 requires at least 32 bytes)
    private static final String SECRET_KEY =
            "my_super_secret_key_which_is_at_least_32_bytes_long_12345";
    private static final long EXPIRATION_TIME = 1000L * 60 * 60; // 1 hour

    private JwtUtil() {
        // Prevent instantiation
    }

    private static Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateToken(final AppUser user) {
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

    public static String generateToken(final String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public static String extractUsername(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public static boolean validateToken(final String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        try {
            final Jws<Claims> claims =
                    Jwts.parserBuilder()
                            .setSigningKey(getSigningKey())
                            .build()
                            .parseClaimsJws(token);

            return !claims.getBody().getExpiration().before(new Date());
        } catch (ExpiredJwtException
                 | UnsupportedJwtException
                 | MalformedJwtException
                 | SignatureException
                 | IllegalArgumentException e) {
            return false;
        }
    }
}
