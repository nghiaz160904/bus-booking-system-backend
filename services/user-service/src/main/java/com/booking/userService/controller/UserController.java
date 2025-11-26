package com.booking.userService.controller;

import com.booking.userService.dto.LoginRequest;
import com.booking.userService.dto.RegisterRequest;
import com.booking.userService.dto.LoginResponse;
import com.booking.userService.dto.UserResponse; 
import com.booking.userService.service.JwtService;
import com.booking.userService.service.UserService;
import com.booking.userService.model.User;
import jakarta.servlet.http.Cookie; 
import jakarta.servlet.http.HttpServletResponse; 
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal; 
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    // --- Access token validity (15 minutes) ---
    private final long ACCESS_TOKEN_VALIDITY_SECONDS = 900; 
    // --- Refresh token validity (7 days) ---
    private final long REFRESH_TOKEN_VALIDITY_SECONDS = 604800;

    @Autowired
    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("{ \"message\": \"User registered successfully\"}");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response // Inject response
    ) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = (User) userService.loadUserByUsername(request.getEmail());
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Save refresh token to DB
        userService.saveUserRefreshToken(user, refreshToken);

        // --- Set cookies ---
        setSecureHttpOnlyCookie(response, "refreshToken", refreshToken, REFRESH_TOKEN_VALIDITY_SECONDS);

        // Return user profile (like getMe)
        UserResponse userResp = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );

        return ResponseEntity.ok(new LoginResponse(accessToken, userResp));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(
            @CookieValue(name = "refreshToken") String requestRefreshToken, // Read from cookie
            HttpServletResponse response // Inject response
    ) {
        return userService.findByRefreshToken(requestRefreshToken)
                .filter(user -> jwtService.isTokenValid(requestRefreshToken, user))
                .map(user -> {
                    String newAccessToken = jwtService.generateAccessToken(user);
                    String newRefreshToken = jwtService.generateRefreshToken(user);

                    userService.saveUserRefreshToken(user, newRefreshToken);
                    setSecureHttpOnlyCookie(response, "refreshToken", newRefreshToken, REFRESH_TOKEN_VALIDITY_SECONDS);

                    UserResponse userResp = new UserResponse(
                            user.getId(),
                            user.getEmail(),
                            user.getRole(),
                            user.getCreatedAt()
                    );

                    return ResponseEntity.ok(new LoginResponse(newAccessToken, userResp));
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
        HttpServletResponse response,
        @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {
        // --- Delete token from DB if it exists ---
        if (refreshToken != null && !refreshToken.isEmpty()) {
            userService.deleteRefreshToken(refreshToken);
        }

        // --- Clear cookies ---
        clearCookie(response, "refreshToken");
        // We could also clear the token from the DB, but clearing the cookie is sufficient
        return ResponseEntity.ok("{ \"message\": \"Logged out successfully\"}");
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(
            @AuthenticationPrincipal User currentUser
    ) {
        UserResponse userResponse = new UserResponse(
                currentUser.getId(),
                currentUser.getEmail(),
                currentUser.getRole(),
                currentUser.getCreatedAt()
        );
        return ResponseEntity.ok(userResponse);
    }

    // --- Helper methods ---

    private void clearCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0) // Expire immediately
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void setSecureHttpOnlyCookie(HttpServletResponse response, String name, String value, long maxAgeInSeconds) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecure) // Configurable (true for HTTPS, false for localhost dev)
                .path("/")
                .maxAge(maxAgeInSeconds)
                .sameSite("Lax") // Protects against CSRF while allowing normal navigation
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}