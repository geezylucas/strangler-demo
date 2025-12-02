package com.example.stranglerdemo.service;

import com.example.stranglerdemo.client.LegacyApiClient;
import com.example.stranglerdemo.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * User Service implementing the Strangler Pattern
 * 
 * STRANGLER PATTERN EXPLANATION:
 * ===============================
 * This service acts as a facade that:
 * 1. Currently proxies requests to the legacy system (jsonplaceholder API)
 * 2. In the future, can gradually migrate functionality to the new system
 * 3. Allows us to add new features without modifying the legacy system
 * 
 * Migration Strategy:
 * - Phase 1 (CURRENT): Route all requests to legacy system
 * - Phase 2: Add caching layer to reduce load on legacy system  
 * - Phase 3: Implement new business logic and enhancements
 * - Phase 4: Migrate data to new database
 * - Phase 5: Gradually switch reads to new system
 * - Phase 6: Switch writes to new system
 * - Phase 7: Decommission legacy system
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final LegacyApiClient legacyApiClient;

    /**
     * Get all users - Currently proxying to legacy system
     * TODO: Phase 2 - Add caching
     * TODO: Phase 5 - Switch to new database for reads
     */
    public List<User> getAllUsers() {
        log.info("Retrieving all users (from legacy system)");
        
        // In a real Strangler implementation, you might have:
        // if (featureToggle.isNewSystemEnabled()) {
        //     return newUserRepository.findAll();
        // } else {
        //     return legacyApiClient.getAllUsers();
        // }
        
        return legacyApiClient.getAllUsers();
    }

    /**
     * Get user by ID - Currently proxying to legacy system
     * TODO: Phase 3 - Add data enrichment from new sources
     * TODO: Phase 5 - Switch to new database for reads
     */
    public User getUserById(Long id) {
        log.info("Retrieving user with id: {} (from legacy system)", id);
        
        User user = legacyApiClient.getUserById(id);
        
        // Here you could add new functionality without touching the legacy system
        // Example: Enrich user data with information from new services
        // user.setAdditionalInfo(newServiceClient.getAdditionalUserInfo(id));
        
        return user;
    }

    /**
     * Get users by city - NEW FEATURE that doesn't exist in legacy system
     * This demonstrates how we can add new capabilities without modifying legacy code
     */
    public List<User> getUsersByCity(String city) {
        log.info("Retrieving users from city: {} (new feature)", city);
        
        // Get all users from legacy system
        List<User> allUsers = legacyApiClient.getAllUsers();
        
        // Apply new business logic
        return allUsers.stream()
                .filter(user -> user.getAddress() != null && 
                               city.equalsIgnoreCase(user.getAddress().getCity()))
                .toList();
    }
}
