package com.example.SmartQueueManagement.model;

import javax.persistence.*;
import lombok.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "queue_tokens")
public class QueueEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String patientId;
    private String patientName;
    private String doctorId;
    private String doctorName;

    @Transient
    private Doctor doctor; // Nested for UI details

    @Transient
    private User patient; // Added for UI details

    private Integer queueNumber;
    private String tokenCode;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private QueueStatus status = QueueStatus.WAITING;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PriorityLevel priority = PriorityLevel.NORMAL;

    @Builder.Default
    private Integer priorityScore = 0;

    private Integer estimatedWaitTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Builder.Default
    private Date createdAt = new Date();
}
