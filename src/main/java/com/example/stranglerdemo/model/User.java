package com.example.stranglerdemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User DTO - Represents user data from the legacy system
 * This is part of the Strangler Pattern implementation where we're
 * gradually extracting user management functionality from the legacy system
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    private String username;
    private String email;
    private Address address;
    private String website;
    private Company company;
}
