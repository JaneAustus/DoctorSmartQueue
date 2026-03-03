package com.example.SmartQueueManagement.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueEntry {

    @DocumentId
    private String id;

    private String patientId;
    private String patientName;
    private String doctorId;
    private String doctorName;

    private Integer queueNumber;
    private String tokenCode;

    @Builder.Default
    private QueueStatus status = QueueStatus.WAITING;

    @Builder.Default
    private PriorityLevel priority = PriorityLevel.NORMAL;

    @Builder.Default
    private Integer priorityScore = 0;

    private Integer estimatedWaitTime;

    @Builder.Default
    private String createdAt = LocalDateTime.now().toString();
}
