package com.example.SmartQueueManagement.controller;

import com.example.SmartQueueManagement.model.QueueEntry;
import com.example.SmartQueueManagement.model.QueueStatus;
import com.example.SmartQueueManagement.model.PriorityLevel;
import com.example.SmartQueueManagement.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    @PostMapping("/join")
    public ResponseEntity<?> joinQueue(@RequestParam String patientId, @RequestParam String doctorId,
            @RequestParam(required = false) PriorityLevel priority) {
        try {
            QueueEntry entry = queueService.joinQueue(patientId, doctorId, priority);
            return ResponseEntity.ok(entry);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/doctor/{doctorId}")
    public List<QueueEntry> getDoctorQueue(@PathVariable String doctorId) {
        return queueService.getQueueForDoctor(doctorId);
    }

    @GetMapping("/patient/{patientId}")
    public List<QueueEntry> getPatientQueue(@PathVariable String patientId) {
        return queueService.getQueueForPatient(patientId);
    }

    @PutMapping("/{entryId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String entryId, @RequestParam QueueStatus status) {
        try {
            QueueEntry entry = queueService.updateQueueStatus(entryId, status);
            return ResponseEntity.ok(entry);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{entryId}")
    public ResponseEntity<?> getEntryStatus(@PathVariable String entryId) {
        try {
            return ResponseEntity.ok(queueService.getQueueEntryStatus(entryId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
