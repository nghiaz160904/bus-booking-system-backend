package com.booking.userService.service;

import com.booking.userService.model.User;
import com.booking.userService.repository.UserRepository;
import com.booking.userService.dto.RegisterRequest;
import com.booking.userService.exception.EmailAlreadyExistsException;
import com.booking.userService.model.Role;
import com.booking.userService.dto.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    private final UserDetailsServiceImpl userDetailsService;

    @Autowired
    public UserService(
            UserRepository userRepository, 
            PasswordEncoder passwordEncoder,
            UserDetailsServiceImpl userDetailsService 
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    public User registerUser(RegisterRequest request) {
        // 1. Check for existing email
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            throw new EmailAlreadyExistsException("Email " + request.getEmail() + " already taken");
        });

        // 2. Hash password before saving
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // 3. Create new user object
        User newUser = User.builder()
                .email(request.getEmail())
                .password(hashedPassword)
                .role(Role.USER)
                .build();
        
        // 4. Save to database
        return userRepository.save(newUser);
    }

    /**
     * This method is used by the AuthenticationManager to load the user
     * for login/password checks.
     */
    public UserDetails loadUserByUsername(String email) {
        // This is now correct and will no longer cause an error
        return userDetailsService.loadUserByUsername(email);
    }

    // --- Method to save the refresh token ---
    public User saveUserRefreshToken(User user, String refreshToken) {
        user.setRefreshToken(refreshToken);
        return userRepository.save(user);
    }

    // --- Method to find user by refresh token ---
    public Optional<User> findByRefreshToken(String refreshToken) {
        return userRepository.findByRefreshToken(refreshToken);
    }

    /**
     * Fetches all users and converts them to a safe DTO.
     * @return A list of UserResponse objects.
     */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getRole(),
                        user.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}