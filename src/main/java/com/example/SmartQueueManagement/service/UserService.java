package com.example.SmartQueueManagement.service;

import com.example.SmartQueueManagement.dto.UserRegistrationDto;
import com.example.SmartQueueManagement.model.User;
import com.example.SmartQueueManagement.model.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final List<User> users = new CopyOnWriteArrayList<>();

    @Autowired
    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;

        // Add a default patient user
        users.add(User.builder()
                .id("test-id-1")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.PATIENT)
                .build());

        // Add a default doctor user
        users.add(User.builder()
                .id("doc-id-1")
                .email("doctor@example.com")
                .firstName("John")
                .lastName("Smith")
                .password(passwordEncoder.encode("doctor123"))
                .role(UserRole.DOCTOR)
                .build());

        users.add(User.builder()
                .id("doc-id-2")
                .email("jones@example.com")
                .firstName("David")
                .lastName("Jones")
                .password(passwordEncoder.encode("doctor123"))
                .role(UserRole.DOCTOR)
                .build());

        users.add(User.builder()
                .id("doc-id-3")
                .email("taylor@example.com")
                .firstName("Emma")
                .lastName("Taylor")
                .password(passwordEncoder.encode("doctor123"))
                .role(UserRole.DOCTOR)
                .build());

        // Add a default admin user
        users.add(User.builder()
                .id("admin-id-1")
                .email("admin@example.com")
                .firstName("System")
                .lastName("Admin")
                .password(passwordEncoder.encode("admin123"))
                .role(UserRole.ADMIN)
                .build());
    }

    public User registerUser(UserRegistrationDto registrationDto) {
        if (users.stream().anyMatch(u -> u.getEmail().equals(registrationDto.getEmail()))) {
            throw new RuntimeException("Email already in use");
        }

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .firstName(registrationDto.getFirstName())
                .lastName(registrationDto.getLastName())
                .email(registrationDto.getEmail())
                .phone(registrationDto.getPhone())
                .password(passwordEncoder.encode(registrationDto.getPassword()))
                .role(UserRole.PATIENT)
                .build();

        users.add(user);
        return user;
    }

    public User authenticate(String email, String password) {
        return users.stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .filter(u -> passwordEncoder.matches(password, u.getPassword()))
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
    }

    public User findByEmail(String email) {
        return users.stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }
}
