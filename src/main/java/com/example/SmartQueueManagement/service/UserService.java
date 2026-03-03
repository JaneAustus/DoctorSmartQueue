package com.example.SmartQueueManagement.service;

import com.example.SmartQueueManagement.dto.UserRegistrationDto;
import com.example.SmartQueueManagement.model.User;
import com.example.SmartQueueManagement.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final FirestoreService firestoreService;
    private final PasswordEncoder passwordEncoder;
    private static final String COLLECTION = "users";

    public User registerUser(UserRegistrationDto registrationDto) throws ExecutionException, InterruptedException {
        // Check if user exists
        List<User> existing = firestoreService.query(COLLECTION, "email", registrationDto.getEmail(), User.class);
        if (!existing.isEmpty()) {
            throw new RuntimeException("Email already in use");
        }

        User user = User.builder()
                .firstName(registrationDto.getFirstName())
                .lastName(registrationDto.getLastName())
                .email(registrationDto.getEmail())
                .phone(registrationDto.getPhone())
                .password(passwordEncoder.encode(registrationDto.getPassword()))
                .role(UserRole.PATIENT)
                .build();

        String id = firestoreService.saveWithAutoId(COLLECTION, user);
        user.setId(id);
        return user;
    }

    public User authenticate(String email, String password) throws ExecutionException, InterruptedException {
        List<User> users = firestoreService.query(COLLECTION, "email", email, User.class);
        if (users.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }

        User user = users.get(0);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        return user;
    }

    public User findByEmail(String email) throws ExecutionException, InterruptedException {
        List<User> users = firestoreService.query(COLLECTION, "email", email, User.class);
        return users.isEmpty() ? null : users.get(0);
    }
}
