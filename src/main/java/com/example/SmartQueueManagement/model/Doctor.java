package com.example.SmartQueueManagement.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {

    private String id;

    private String name;
    private String email; // Added for login/bridge
    private String specialization;
    private String roomNumber;
    private String departmentId;
    private Department department; // Nested for UI

    @Builder.Default
    private boolean isAvailable = true;

    @Builder.Default
    private int avgConsultationTimeMinutes = 15;
}
