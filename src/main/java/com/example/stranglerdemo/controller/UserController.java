package com.example.stranglerdemo.controller;

import com.example.stranglerdemo.model.User;
import com.example.stranglerdemo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for User operations
 * This is the new API facade that clients will use instead of directly calling the legacy system
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Get all users
     * GET /api/v1/users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("GET /api/v1/users - Fetching all users (new feature)");
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by ID
     * GET /api/v1/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        log.info("GET /api/v1/users/{} - Fetching user (missing feature flag)", id);
        User user = userService.getUserById(id);
        
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(user);
    }

    /**
     * Get users by city - NEW FEATURE not available in legacy system
     * GET /api/v1/users/city/{city}
     * 
     * This demonstrates how we can add new functionality without modifying the legacy system
     */
    @GetMapping("/city/{city}")
    public ResponseEntity<List<User>> getUsersByCity(@PathVariable String city) {
        log.info("GET /api/v1/users/city/{} - Fetching users by city (new feature)", city);
        List<User> users = userService.getUsersByCity(city);
        return ResponseEntity.ok(users);
    }

    /**
     * Health check endpoint
     * GET /api/v1/users/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User service is healthy");
    }
}
