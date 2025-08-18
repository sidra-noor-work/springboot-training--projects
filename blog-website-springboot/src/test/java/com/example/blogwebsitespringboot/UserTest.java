package com.example.blogwebsitespringboot;



import com.example.blogwebsitespringboot.jwt.JwtUtil;
import com.example.blogwebsitespringboot.model.AppUser;
import com.example.blogwebsitespringboot.repository.UserRepository;
import com.example.blogwebsitespringboot.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserTest {

    private UserRepository userRepository;
    private JwtUtil jwtUtil;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        jwtUtil = mock(JwtUtil.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserService(userRepository, jwtUtil, passwordEncoder);
    }

    @Test
    void register_shouldReturnTrue_whenUserNotExists() {
        AppUser user = new AppUser();
        user.setUsername("testuser");
        user.setPassword("password");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        boolean result = userService.register(user);

        assertTrue(result);
        verify(userRepository, times(1)).save(any(AppUser.class));
    }

    @Test
    void register_shouldReturnFalse_whenUserAlreadyExists() {
        AppUser user = new AppUser();
        user.setUsername("existingUser");

        when(userRepository.findByUsername("existingUser")).thenReturn(Optional.of(new AppUser()));

        boolean result = userService.register(user);

        assertFalse(result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void authenticate_shouldReturnToken_whenPasswordMatches() {
        AppUser user = new AppUser();
        user.setUsername("user");
        user.setPassword("encodedPass");

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("rawPass", "encodedPass")).thenReturn(true);
        when(jwtUtil.generateToken(user)).thenReturn("mockToken");

        String token = userService.authenticate("user", "rawPass");

        assertEquals("mockToken", token);
    }

    @Test
    void authenticate_shouldReturnNull_whenPasswordDoesNotMatch() {
        AppUser user = new AppUser();
        user.setUsername("user");
        user.setPassword("encodedPass");

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPass", "encodedPass")).thenReturn(false);

        String token = userService.authenticate("user", "wrongPass");

        assertNull(token);
    }

    @Test
    void findByUsername_shouldReturnUser_whenUserExists() {
        AppUser user = new AppUser();
        user.setUsername("findme");

        when(userRepository.findByUsername("findme")).thenReturn(Optional.of(user));

        AppUser result = userService.findByUsername("findme");

        assertEquals("findme", result.getUsername());
    }

    @Test
    void findByUsername_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userService.findByUsername("missing");
        });
    }

    @Test
    void isOAuthUser_shouldReturnTrue_ifPasswordEmpty() {
        AppUser user = new AppUser();
        user.setUsername("oauthUser");
        user.setPassword("");

        when(userRepository.findByUsername("oauthUser")).thenReturn(Optional.of(user));

        assertTrue(userService.isOAuthUser("oauthUser"));
    }
    @Test
    void loadUserByUsername_returnsUserDetails_whenUserExists() {
        AppUser user = new AppUser();
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setRole("USER");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(
                a -> a.getAuthority().equals("ROLE_USER"))
        );

        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_throwsException_whenUserNotFound() {
        when(userRepository.findByUsername("notfound")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("notfound");
        });

        verify(userRepository, times(1)).findByUsername("notfound");
    }
    @Test
    void isOAuthUser_shouldReturnFalse_ifPasswordPresent() {
        AppUser user = new AppUser();
        user.setUsername("normalUser");
        user.setPassword("hashed");

        when(userRepository.findByUsername("normalUser")).thenReturn(Optional.of(user));

        assertFalse(userService.isOAuthUser("normalUser"));
    }

    @Test
    void authenticate_shouldReturnNull_whenUserNotFound() {
        when(userRepository.findByUsername("nouser")).thenReturn(Optional.empty());

        String token = userService.authenticate("nouser", "anyPass");

        assertNull(token);
    }

    @Test
    void authenticate_shouldPropagateException_whenJwtUtilThrows() {
        AppUser user = new AppUser();
        user.setUsername("user");
        user.setPassword("encodedPass");

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("rawPass", "encodedPass")).thenReturn(true);
        when(jwtUtil.generateToken(user)).thenThrow(new RuntimeException("JWT error"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.authenticate("user", "rawPass");
        });

        assertEquals("JWT error", exception.getMessage());
    }

    @Test
    void isOAuthUser_shouldReturnFalse_whenUserNotFound() {
        when(userRepository.findByUsername("unknownUser")).thenReturn(Optional.empty());

        boolean result = userService.isOAuthUser("unknownUser");

        assertFalse(result);
    }

    @Test
    void findByUsername_shouldThrowException_whenRepositoryThrows() {
        when(userRepository.findByUsername("errorUser")).thenThrow(new RuntimeException("DB error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.findByUsername("errorUser");
        });

        assertEquals("DB error", exception.getMessage());
    }


}
