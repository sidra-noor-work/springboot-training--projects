package com.example.blogwebsitespringboot.service;

import com.example.blogwebsitespringboot.model.AppUser;
import com.example.blogwebsitespringboot.jwt.JwtUtil;
import com.example.blogwebsitespringboot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(final UserRepository userRepository,
                       final JwtUtil jwtUtil,
                       final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean register(final AppUser user) {
        boolean registered;
        final boolean exists = userRepository.findByUsername(user.getUsername()).isPresent();
        if (!exists) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRole("USER");
            userRepository.save(user);
            registered = true;
        } else {
            registered = false;
        }
        return registered;
    }

    public String authenticate(final String username, final String rawPassword) {
        String token;
        try {
            final AppUser foundUser = findByUsername(username);
            if (passwordEncoder.matches(rawPassword, foundUser.getPassword())) {
                token = jwtUtil.generateToken(foundUser);
            } else {
                token = null;
            }
        } catch (UsernameNotFoundException e) {
            logger.warn("Authentication failed: {}", e.getMessage());
            token = null;
        }
        return token;
    }

    public AppUser save(final AppUser user) {
        return userRepository.save(user);
    }

    public AppUser findByUsername(final String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username));
    }

    public void registerOAuthUserIfNeeded(final String username) {
        if (!userRepository.existsByUsername(username)) {
            final AppUser user = new AppUser();
            user.setUsername(username);
            user.setPassword(""); // No password for OAuth users
            user.setRole("USER");
            userRepository.save(user);
        }
    }

    public boolean userExists(final String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public boolean isOAuthUser(final String username) {
        final AppUser user = userRepository.findByUsername(username).orElse(null);
        return user != null && (user.getPassword() == null || user.getPassword().isEmpty());
    }

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        final AppUser user = findByUsername(username);
        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }
}
