package com.example.SmartQueueManagement.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {

    @DocumentId
    private String id;

    private String name;
    private String email; // Added for login/bridge
    private String specialization;
    private String roomNumber;

    @Builder.Default
    private boolean isAvailable = true;

    @Builder.Default
    private int avgConsultationTimeMinutes = 15;

    private String departmentId; // Reference to Department
}
