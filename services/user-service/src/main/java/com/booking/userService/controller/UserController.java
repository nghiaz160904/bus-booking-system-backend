package com.booking.userService.controller;

import com.booking.userService.dto.LoginRequest;
import com.booking.userService.dto.LoginResponse;
import com.booking.userService.dto.RegisterRequest;
import com.booking.userService.service.JwtService;
import com.booking.userService.service.UserService;
import com.booking.userService.model.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService; 

    @Autowired
    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        // @Valid triggers the validation in the RegisterRequest DTO
        userService.registerUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body("{ \"message\": \"User registered successfully\"}");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // 1. This will check if the email and password are correct
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. If correct, find the user and generate a token
        var user = (User) userService.loadUserByUsername(request.getEmail()); // We know this is our User object
        String token = jwtService.generateToken(user);

        // 3. Return the token
        return ResponseEntity.ok(new LoginResponse(token));
    }
}