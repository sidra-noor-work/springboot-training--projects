package com.example.blog_website_springboot.Service;

import com.example.blog_website_springboot.Model.AppUser;
import com.example.blog_website_springboot.JWT.JwtUtil;
import com.example.blog_website_springboot.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean register(AppUser user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        userRepository.save(user);
        return true;
    }
    public AppUser save(AppUser user) {
        return userRepository.save(user);
    }

    public String authenticate(String username, String rawPassword) {
        AppUser user;
        try {
            user = findByUsername(username);
        } catch (UsernameNotFoundException e) {
            return null;
        }

        if (passwordEncoder.matches(rawPassword, user.getPassword())) {
            return jwtUtil.generateToken(user);
        }
        return null;
    }

    public AppUser findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    public void registerOAuthUserIfNeeded(String username) {
        if (!userRepository.existsByUsername(username)) {
            AppUser user = new AppUser();
            user.setUsername(username);
            user.setPassword(""); // no password for GitHub users
            user.setRole("USER");
            userRepository.save(user);
        }
    }

    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public boolean isOAuthUser(String username) {
        AppUser user = userRepository.findByUsername(username).orElse(null);
        return user != null && (user.getPassword() == null || user.getPassword().isEmpty());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = findByUsername(username);
        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole()) // Use actual role
                .build();
    }
}
