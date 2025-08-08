package com.example.blog_website_springboot.Service;

import com.example.blog_website_springboot.JWT.JwtUtil;
import com.example.blog_website_springboot.Model.AppUser;
import com.example.blog_website_springboot.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
        import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private AppUser testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new AppUser();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setRole("USER");
    }

    @Test
    void registerNewUser_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(AppUser.class))).thenReturn(testUser);

        AppUser newUser = new AppUser();
        newUser.setUsername("testuser");
        newUser.setPassword("password");

        boolean result = userService.register(newUser);

        assertThat(result).isTrue();
        verify(userRepository).save(any(AppUser.class));
    }
    @Test
    void registerOAuthUserIfNeeded_UserDoesNotExist_CreatesUser() {
        when(userRepository.existsByUsername("oauthUser")).thenReturn(false);

        userService.registerOAuthUserIfNeeded("oauthUser");

        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("oauthUser") &&
                        user.getPassword().isEmpty() &&
                        user.getRole().equals("USER")
        ));
    }

    @Test
    void registerOAuthUserIfNeeded_UserExists_DoesNothing() {
        when(userRepository.existsByUsername("oauthUser")).thenReturn(true);

        userService.registerOAuthUserIfNeeded("oauthUser");

        verify(userRepository, never()).save(any());
    }

    @Test
    void save_ShouldCallRepositorySave() {
        when(userRepository.save(testUser)).thenReturn(testUser);

        AppUser saved = userService.save(testUser);

        assertThat(saved).isEqualTo(testUser);
        verify(userRepository).save(testUser);
    }

    @Test
    void authenticate_UserNotFound_ReturnsNull() {
        when(userRepository.findByUsername("nouser")).thenReturn(Optional.empty());

        String token = userService.authenticate("nouser", "any");

        assertThat(token).isNull();
    }

    @Test
    void registerExistingUser_Failure() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        boolean result = userService.register(testUser);

        assertThat(result).isFalse();
        verify(userRepository, never()).save(any(AppUser.class));
    }

    @Test
    void authenticateWithValidCredentials_ReturnsToken() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(testUser)).thenReturn("jwt-token");

        String token = userService.authenticate("testuser", "password");

        assertThat(token).isEqualTo("jwt-token");
    }

    @Test
    void authenticateWithInvalidPassword_ReturnsNull() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpass", "encodedPassword")).thenReturn(false);

        String token = userService.authenticate("testuser", "wrongpass");

        assertThat(token).isNull();
    }

    @Test
    void findByUsername_UserExists_ReturnsUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        AppUser foundUser = userService.findByUsername("testuser");

        assertThat(foundUser).isEqualTo(testUser);
    }

    @Test
    void findByUsername_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByUsername("nonexistent"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with username");
    }

    @Test
    void loadUserByUsername_ReturnsSpringSecurityUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userService.loadUserByUsername("testuser");

        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDetails.getAuthorities()).hasSize(1);
    }

    @Test
    void userExists_ReturnsTrue() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        boolean exists = userService.userExists("testuser");

        assertThat(exists).isTrue();
    }

    @Test
    void userExists_ReturnsFalse() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        boolean exists = userService.userExists("nonexistent");

        assertThat(exists).isFalse();
    }

    @Test
    void isOAuthUser_WithEmptyPassword_ReturnsTrue() {
        testUser.setPassword("");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        boolean result = userService.isOAuthUser("testuser");

        assertThat(result).isTrue();
    }

    @Test
    void isOAuthUser_WithNonEmptyPassword_ReturnsFalse() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        boolean result = userService.isOAuthUser("testuser");

        assertThat(result).isFalse();
    }
}
