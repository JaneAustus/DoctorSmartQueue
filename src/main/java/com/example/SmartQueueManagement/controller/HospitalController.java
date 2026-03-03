package com.example.SmartQueueManagement.controller;

import com.example.SmartQueueManagement.model.*;
import com.example.SmartQueueManagement.service.FirestoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/hospital")
@RequiredArgsConstructor
public class HospitalController {

    private final FirestoreService firestoreService;

    @GetMapping("/departments")
    public List<Department> getAllDepartments() throws ExecutionException, InterruptedException {
        return firestoreService.getAll("departments", Department.class);
    }

    @GetMapping("/doctors")
    public List<Doctor> getAllDoctors() throws ExecutionException, InterruptedException {
        return firestoreService.getAll("doctors", Doctor.class);
    }

    @GetMapping("/stats")
    public Map<String, Long> getStats() throws ExecutionException, InterruptedException {
        Map<String, Long> stats = new HashMap<>();
        List<Doctor> doctors = firestoreService.getAll("doctors", Doctor.class);
        List<Department> departments = firestoreService.getAll("departments", Department.class);
        List<QueueEntry> queue = firestoreService.getAll("queue_entries", QueueEntry.class);

        stats.put("totalDoctors", (long) doctors.size());
        stats.put("totalDepts", (long) departments.size());
        stats.put("totalWaiting", queue.stream().filter(e -> e.getStatus() == QueueStatus.WAITING).count());

        return stats;
    }

    @GetMapping("/live-overview")
    public List<Map<String, Object>> getLiveOverview() throws ExecutionException, InterruptedException {
        List<Doctor> doctors = firestoreService.getAll("doctors", Doctor.class);
        List<QueueEntry> allQueue = firestoreService.getAll("queue_entries", QueueEntry.class);
        List<Map<String, Object>> overview = new ArrayList<>();

        for (Doctor doctor : doctors) {
            Map<String, Object> data = new HashMap<>();
            data.put("doctorName", doctor.getName());
            data.put("specialization", doctor.getSpecialization());

            // Current Serving
            allQueue.stream()
                    .filter(e -> e.getDoctorId().equals(doctor.getId()) && e.getStatus() == QueueStatus.IN_PROGRESS)
                    .findFirst()
                    .ifPresent(entry -> data.put("servingToken", entry.getQueueNumber()));

            // Waiting count
            long waitingCount = allQueue.stream()
                    .filter(e -> e.getDoctorId().equals(doctor.getId()) && e.getStatus() == QueueStatus.WAITING)
                    .count();
            data.put("waitingCount", waitingCount);

            overview.add(data);
        }
        return overview;
    }
}
