package com.example.SmartQueueManagement.controller;

import com.example.SmartQueueManagement.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/hospital")
@RequiredArgsConstructor
public class HospitalController {

    @GetMapping("/departments")
    public List<Department> getAllDepartments() {
        return Arrays.asList(
                Department.builder().id("dept-1").name("Cardiology").code("CARD").build(),
                Department.builder().id("dept-2").name("Neurology").code("NEURO").build(),
                Department.builder().id("dept-3").name("Pediatrics").code("PEDS").build());
    }

    @GetMapping("/doctors")
    public List<Doctor> getAllDoctors() {
        return Arrays.asList(
                Doctor.builder().id("doc-1").name("Dr. Smith").email("doctor@example.com")
                        .specialization("Cardiologist")
                        .roomNumber("101")
                        .department(Department.builder().name("Cardiology").build()).build(),
                Doctor.builder().id("doc-2").name("Dr. Jones").email("jones@example.com").specialization("Neurologist")
                        .roomNumber("102")
                        .department(Department.builder().name("Neurology").build()).build(),
                Doctor.builder().id("doc-3").name("Dr. Taylor").email("taylor@example.com")
                        .specialization("Pediatrician")
                        .roomNumber("103")
                        .department(Department.builder().name("Pediatrics").build()).build());
    }

    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalDoctors", 3L);
        stats.put("totalDepts", 3L);
        stats.put("totalWaiting", 5L);
        return stats;
    }

    @GetMapping("/live-overview")
    public List<Map<String, Object>> getLiveOverview() {
        List<Map<String, Object>> overview = new ArrayList<>();

        Map<String, Object> doc1 = new HashMap<>();
        doc1.put("doctorName", "Dr. Smith");
        doc1.put("doctorEmail", "doctor@example.com");
        doc1.put("specialization", "Cardiologist");
        doc1.put("servingToken", 12);
        doc1.put("waitingCount", 3);
        overview.add(doc1);

        Map<String, Object> doc2 = new HashMap<>();
        doc2.put("doctorName", "Dr. Jones");
        doc2.put("doctorEmail", "jones@example.com");
        doc2.put("specialization", "Neurologist");
        doc2.put("servingToken", 5);
        doc2.put("waitingCount", 2);
        overview.add(doc2);

        return overview;
    }
}
