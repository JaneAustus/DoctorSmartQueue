package com.example.SmartQueueManagement.service;

import com.example.SmartQueueManagement.model.*;
import com.example.SmartQueueManagement.repository.QueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QueueService {

        private final QueueRepository queueRepository;
        private final UserService userService;

        @Transactional
        public QueueEntry joinQueue(String patientId, String doctorId, PriorityLevel priority) {
                User patient = userService.findByEmail(patientId);
                if (patient == null) {
                        patient = User.builder().firstName("Guest").lastName("Patient").email(patientId).build();
                }

                List<QueueEntry> doctorQueue = queueRepository.findByDoctorId(doctorId);
                int nextNumber = (int) doctorQueue.stream()
                                .filter(e -> e.getStatus() != QueueStatus.CANCELLED)
                                .count() + 1;

                String tokenCode = "TOKEN-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
                PriorityLevel p = (priority != null) ? priority : PriorityLevel.NORMAL;

                // Mock doctor info based on ID (to populate Transient fields if needed)
                Doctor mockDoctor = getMockDoctor(doctorId);

                QueueEntry entry = QueueEntry.builder()
                                .patientId(patientId)
                                .patientName(patient.getFirstName() + " " + patient.getLastName())
                                .doctorId(doctorId)
                                .doctorName(mockDoctor.getName())
                                .doctor(mockDoctor)
                                .patient(patient)
                                .queueNumber(nextNumber)
                                .tokenCode(tokenCode)
                                .status(QueueStatus.WAITING)
                                .priority(p)
                                .priorityScore(p.getBaseWeight())
                                .createdAt(new Date())
                                .estimatedWaitTime(nextNumber * 15)
                                .build();

                return queueRepository.save(entry);
        }

        public List<QueueEntry> getQueueForDoctor(String doctorId) {
                List<QueueEntry> entries = queueRepository.findByDoctorId(doctorId).stream()
                                .filter(e -> e.getStatus() == QueueStatus.WAITING)
                                .sorted(Comparator.comparing(QueueEntry::getPriorityScore).reversed()
                                                .thenComparing(QueueEntry::getCreatedAt))
                                .collect(Collectors.toList());

                // Populate transient fields
                entries.forEach(e -> {
                        e.setDoctor(getMockDoctor(e.getDoctorId()));
                        e.setPatient(userService.findByEmail(e.getPatientId()));
                });
                return entries;
        }

        public List<QueueEntry> getQueueForPatient(String patientId) {
                List<QueueEntry> entries = queueRepository.findByPatientIdIgnoreCase(patientId).stream()
                                .sorted(Comparator.comparing(QueueEntry::getCreatedAt).reversed())
                                .collect(Collectors.toList());

                entries.forEach(e -> {
                        e.setDoctor(getMockDoctor(e.getDoctorId()));
                });
                return entries;
        }

        @Transactional
        public QueueEntry updateQueueStatus(String entryId, QueueStatus status) {
                QueueEntry entry = queueRepository.findById(Long.parseLong(entryId))
                                .orElseThrow(() -> new RuntimeException("Queue entry not found"));
                entry.setStatus(status);
                return queueRepository.save(entry);
        }

        public QueueEntry getQueueEntryStatus(String entryId) {
                QueueEntry entry = queueRepository.findById(Long.parseLong(entryId))
                                .orElseThrow(() -> new RuntimeException("Queue entry not found"));
                entry.setDoctor(getMockDoctor(entry.getDoctorId()));
                return entry;
        }

        @org.springframework.scheduling.annotation.Scheduled(fixedRate = 60000)
        @Transactional
        public void applyDynamicAging() {
                Date now = new Date();
                List<QueueEntry> allWaiting = queueRepository.findAll().stream()
                                .filter(e -> e.getStatus() == QueueStatus.WAITING)
                                .collect(Collectors.toList());

                for (QueueEntry entry : allWaiting) {
                        long diff = now.getTime() - entry.getCreatedAt().getTime();
                        long minutesWaiting = diff / (60 * 1000);
                        if (minutesWaiting > 30) {
                                int bonus = (int) ((minutesWaiting - 30) / 10) * 5;
                                entry.setPriorityScore(entry.getPriority().getBaseWeight() + bonus);
                        }
                }
                queueRepository.saveAll(allWaiting);
        }

        private Doctor getMockDoctor(String doctorId) {
                String doctorName = "Dr. Smith";
                String specialty = "Cardiologist";
                String room = "101";
                String dept = "Cardiology";

                if ("doc-2".equals(doctorId)) {
                        doctorName = "Dr. Jones";
                        specialty = "Neurologist";
                        room = "102";
                        dept = "Neurology";
                } else if ("doc-3".equals(doctorId)) {
                        doctorName = "Dr. Taylor";
                        specialty = "Pediatrician";
                        room = "103";
                        dept = "Pediatrics";
                }
                return Doctor.builder().id(doctorId).name(doctorName).specialization(specialty)
                                .roomNumber(room).department(Department.builder().name(dept).build()).build();
        }
}
