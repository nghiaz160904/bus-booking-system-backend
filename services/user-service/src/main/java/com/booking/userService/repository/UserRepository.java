package com.booking.userService.repository;

import com.booking.userService.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Spring Data JPA automatically creates this query for us
    // "SELECT * FROM users WHERE email = ?"
    Optional<User> findByEmail(String email);
}