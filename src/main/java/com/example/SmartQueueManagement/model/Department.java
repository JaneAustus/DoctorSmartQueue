package com.example.SmartQueueManagement.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    private String id;

    private String name;
    private String code; // e.g., CARD for Cardiology
}
