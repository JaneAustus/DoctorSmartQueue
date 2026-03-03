package com.example.SmartQueueManagement.service;

import com.example.SmartQueueManagement.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QueueService {

        private final FirestoreService firestoreService;
        private static final String QUEUE_COLLECTION = "queue_entries";
        private static final String DOCTOR_COLLECTION = "doctors";
        private static final String USER_COLLECTION = "users";

        public Doctor findLeastBusyDoctorInDepartment(String departmentId)
                        throws ExecutionException, InterruptedException {
                List<Doctor> doctors = firestoreService.query(DOCTOR_COLLECTION, "departmentId", departmentId,
                                Doctor.class);
                if (doctors.isEmpty()) {
                        throw new RuntimeException("No doctors found in this department");
                }

                Doctor leastBusyDoctor = null;
                long minCount = Long.MAX_VALUE;

                for (Doctor doctor : doctors) {
                        if (doctor.isAvailable()) {
                                List<QueueEntry> waiting = firestoreService
                                                .query(QUEUE_COLLECTION, "doctorId", doctor.getId(), QueueEntry.class)
                                                .stream().filter(e -> e.getStatus() == QueueStatus.WAITING)
                                                .collect(Collectors.toList());

                                long count = waiting.size();
                                if (count < minCount) {
                                        minCount = count;
                                        leastBusyDoctor = doctor;
                                }
                        }
                }

                if (leastBusyDoctor == null) {
                        throw new RuntimeException("No available doctors in this department");
                }

                return leastBusyDoctor;
        }

        public QueueEntry joinQueueAutoAssign(String patientId, String departmentId, PriorityLevel priority)
                        throws ExecutionException, InterruptedException {
                Doctor leastBusyDoctor = findLeastBusyDoctorInDepartment(departmentId);
                return joinQueue(patientId, leastBusyDoctor.getId(), priority);
        }

        public QueueEntry joinQueue(String patientId, String doctorId, PriorityLevel priority)
                        throws ExecutionException, InterruptedException {
                User patient = firestoreService.get(USER_COLLECTION, patientId, User.class);
                if (patient == null)
                        throw new RuntimeException("Patient not found");

                Doctor doctor = firestoreService.get(DOCTOR_COLLECTION, doctorId, Doctor.class);
                if (doctor == null)
                        throw new RuntimeException("Doctor not found");

                if (!doctor.isAvailable()) {
                        throw new RuntimeException("Doctor is currently not available");
                }

                // Calculate next queue number (simplified for NoSQL)
                List<QueueEntry> doctorQueue = firestoreService.query(QUEUE_COLLECTION, "doctorId", doctorId,
                                QueueEntry.class);
                int nextNumber = doctorQueue.stream()
                                .filter(e -> e.getStatus() != QueueStatus.CANCELLED)
                                .mapToInt(QueueEntry::getQueueNumber)
                                .max()
                                .orElse(0) + 1;

                String dateStr = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now());
                String tokenCode = String.format("DOC%s-%s-%03d", doctorId.substring(0, Math.min(doctorId.length(), 4)),
                                dateStr, nextNumber);

                PriorityLevel p = (priority != null) ? priority : PriorityLevel.NORMAL;

                QueueEntry entry = QueueEntry.builder()
                                .patientId(patientId)
                                .patientName(patient.getFirstName() + " " + patient.getLastName())
                                .doctorId(doctorId)
                                .doctorName(doctor.getName())
                                .queueNumber(nextNumber)
                                .tokenCode(tokenCode)
                                .status(QueueStatus.WAITING)
                                .priority(p)
                                .priorityScore(p.getBaseWeight())
                                .build();

                String id = firestoreService.saveWithAutoId(QUEUE_COLLECTION, entry);
                entry.setId(id);

                // Set estimated wait time
                long peopleAhead = doctorQueue.stream()
                                .filter(e -> e.getStatus() == QueueStatus.WAITING
                                                && e.getPriorityScore() >= entry.getPriorityScore())
                                .count();
                entry.setEstimatedWaitTime((int) (peopleAhead * doctor.getAvgConsultationTimeMinutes()));

                return entry;
        }

        public List<QueueEntry> getQueueForDoctor(String doctorId) throws ExecutionException, InterruptedException {
                return firestoreService.query(QUEUE_COLLECTION, "doctorId", doctorId, QueueEntry.class)
                                .stream()
                                .filter(e -> e.getStatus() == QueueStatus.WAITING)
                                .sorted(Comparator.comparing(QueueEntry::getPriorityScore).reversed()
                                                .thenComparing(QueueEntry::getCreatedAt))
                                .collect(Collectors.toList());
        }

        public List<QueueEntry> getQueueForPatient(String patientId) throws ExecutionException, InterruptedException {
                return firestoreService.query(QUEUE_COLLECTION, "patientId", patientId, QueueEntry.class)
                                .stream()
                                .sorted(Comparator.comparing(QueueEntry::getCreatedAt).reversed())
                                .collect(Collectors.toList());
        }

        public QueueEntry updateQueueStatus(String entryId, QueueStatus status)
                        throws ExecutionException, InterruptedException {
                QueueEntry entry = firestoreService.get(QUEUE_COLLECTION, entryId, QueueEntry.class);
                if (entry == null)
                        throw new RuntimeException("Queue entry not found");
                entry.setStatus(status);
                firestoreService.save(QUEUE_COLLECTION, entry, entryId);
                return entry;
        }

        public QueueEntry getQueueEntryStatus(String entryId) throws ExecutionException, InterruptedException {
                QueueEntry entry = firestoreService.get(QUEUE_COLLECTION, entryId, QueueEntry.class);
                if (entry == null)
                        throw new RuntimeException("Queue entry not found");

                if (entry.getStatus() == QueueStatus.WAITING) {
                        Doctor doctor = firestoreService.get(DOCTOR_COLLECTION, entry.getDoctorId(), Doctor.class);
                        List<QueueEntry> doctorQueue = firestoreService.query(QUEUE_COLLECTION, "doctorId",
                                        entry.getDoctorId(), QueueEntry.class);
                        long peopleAhead = doctorQueue.stream()
                                        .filter(e -> e.getStatus() == QueueStatus.WAITING
                                                        && e.getPriorityScore() >= entry.getPriorityScore())
                                        .count() - 1; // Exclude self
                        entry.setEstimatedWaitTime(
                                        (int) (Math.max(0, peopleAhead) * doctor.getAvgConsultationTimeMinutes()));
                } else {
                        entry.setEstimatedWaitTime(0);
                }

                return entry;
        }

        @org.springframework.scheduling.annotation.Scheduled(fixedRate = 60000)
        public void applyDynamicAging() throws ExecutionException, InterruptedException {
                List<QueueEntry> waitingEntries = firestoreService.getAll(QUEUE_COLLECTION, QueueEntry.class)
                                .stream().filter(e -> e.getStatus() == QueueStatus.WAITING)
                                .collect(Collectors.toList());

                LocalDateTime now = LocalDateTime.now();
                for (QueueEntry entry : waitingEntries) {
                        LocalDateTime createdAt = LocalDateTime.parse(entry.getCreatedAt());
                        long minutesWaiting = java.time.temporal.ChronoUnit.MINUTES.between(createdAt, now);
                        if (minutesWaiting > 30) {
                                int bonus = (int) ((minutesWaiting - 30) / 10) * 5;
                                entry.setPriorityScore(entry.getPriority().getBaseWeight() + bonus);
                                firestoreService.save(QUEUE_COLLECTION, entry, entry.getId());
                        }
                }
        }
}
