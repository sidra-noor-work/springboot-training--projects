package com.example.blog_website_springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean register(AppUser user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        user.setRole("USER");
        return true;
    }

    public boolean login(String username, String password) {
        Optional<AppUser> userOpt = userRepository.findByUsername(username);
        return userOpt
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(false);
    }

    // 👇 This is required for Spring Security
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }
}
