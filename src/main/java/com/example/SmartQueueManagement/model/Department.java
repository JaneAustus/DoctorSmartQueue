package com.example.SmartQueueManagement.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @DocumentId
    private String id;

    private String name;
    private String code; // e.g., CARD for Cardiology
}
