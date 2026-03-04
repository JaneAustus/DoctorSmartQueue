package com.example.SmartQueueManagement.model;

import lombok.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private String id;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String password;

    @Builder.Default
    private UserRole role = UserRole.PATIENT;

    @Builder.Default
    private Date createdAt = new Date();
}
