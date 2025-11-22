package com.booking.userService.dto;

import com.booking.userService.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

/**
 * A DTO for safely sending user information to the client.
 * This class omits sensitive fields like password and refreshToken.
 */
@Data
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private Role role;
    private Date createdAt;
}