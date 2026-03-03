package com.example.SmartQueueManagement.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @DocumentId
    private String id;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String password;

    @Builder.Default
    private UserRole role = UserRole.PATIENT;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
