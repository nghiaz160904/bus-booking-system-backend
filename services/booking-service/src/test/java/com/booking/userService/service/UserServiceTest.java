package com.booking.userService.service;

import com.booking.userService.dto.RegisterRequest;
import com.booking.userService.dto.UserResponse;
import com.booking.userService.exception.EmailAlreadyExistsException;
import com.booking.userService.model.Role;
import com.booking.userService.model.User;
import com.booking.userService.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.USER)
                .createdAt(new Date())
                .refreshToken("someRefreshToken")
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
    }

    // --- 1. Registration Tests ---

    @Test
    void registerUser_Success() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User savedUser = userService.registerUser(registerRequest);

        // Assert
        assertNotNull(savedUser);
        assertEquals(testUser.getEmail(), savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_EmailAlreadyExists_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.registerUser(registerRequest);
        });

        // Verify save was NEVER called
        verify(userRepository, never()).save(any(User.class));
    }

    // --- 2. Login / UserDetails Tests ---

    @Test
    void loadUserByUsername_Success() {
        // Arrange
        when(userDetailsService.loadUserByUsername(testUser.getEmail())).thenReturn(testUser);

        // Act
        UserDetails foundUser = userService.loadUserByUsername(testUser.getEmail());

        // Assert
        assertNotNull(foundUser);
        assertEquals(testUser.getUsername(), foundUser.getUsername());
    }

    @Test
    void loadUserByUsername_NotFound_ThrowsException() {
        // Arrange
        String email = "notfound@example.com";
        when(userDetailsService.loadUserByUsername(email))
                .thenThrow(new UsernameNotFoundException("User not found"));

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername(email);
        });
    }

    // --- 3. Refresh Token Tests ---

    @Test
    void saveUserRefreshToken_Success() {
        // Arrange
        String newToken = "newRefreshToken123";
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User updatedUser = userService.saveUserRefreshToken(testUser, newToken);

        // Assert
        assertEquals(newToken, updatedUser.getRefreshToken());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void findByRefreshToken_Success() {
        // Arrange
        when(userRepository.findByRefreshToken("someRefreshToken")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> foundUser = userService.findByRefreshToken("someRefreshToken");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getId(), foundUser.get().getId());
    }

    @Test
    void deleteRefreshToken_Success() {
        // Arrange
        String tokenToDelete = "tokenToDelete";
        User userWithToken = User.builder().refreshToken(tokenToDelete).build();
        
        when(userRepository.findByRefreshToken(tokenToDelete)).thenReturn(Optional.of(userWithToken));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        userService.deleteRefreshToken(tokenToDelete);

        // Assert
        assertNull(userWithToken.getRefreshToken()); // Should be null now
        verify(userRepository, times(1)).save(userWithToken);
    }

    @Test
    void deleteRefreshToken_TokenNotFound_DoNothing() {
        // Arrange
        when(userRepository.findByRefreshToken("invalidToken")).thenReturn(Optional.empty());

        // Act
        userService.deleteRefreshToken("invalidToken");

        // Assert
        verify(userRepository, never()).save(any(User.class));
    }

    // --- 4. Get All Users Tests ---

    @Test
    void getAllUsers_Success() {
        // Arrange
        User adminUser = User.builder().id(2L).email("admin@test.com").role(Role.ADMIN).createdAt(new Date()).build();
        List<User> userList = Arrays.asList(testUser, adminUser);
        
        when(userRepository.findAll()).thenReturn(userList);

        // Act
        List<UserResponse> responses = userService.getAllUsers();

        // Assert
        assertEquals(2, responses.size());
        assertEquals(testUser.getEmail(), responses.get(0).getEmail());
        assertEquals(Role.ADMIN, responses.get(1).getRole());
        // Ensure sensitive data is NOT present (UserResponse doesn't have password field, so this is implicitly tested by compilation, but logic check is good)
    }
}