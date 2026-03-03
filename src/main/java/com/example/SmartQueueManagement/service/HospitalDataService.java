package com.example.SmartQueueManagement.service;

import com.example.SmartQueueManagement.model.Department;
import com.example.SmartQueueManagement.model.Doctor;
import com.example.SmartQueueManagement.model.User;
import com.example.SmartQueueManagement.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class HospitalDataService {

        private final FirestoreService firestoreService;
        private final PasswordEncoder passwordEncoder;

        private static final String USER_COL = "users";
        private static final String DEPT_COL = "departments";
        private static final String DOC_COL = "doctors";

        @PostConstruct
        public void initData() {
                try {
                        System.out.println("--- Firestore Initialization Check ---");

                        // Check Users
                        List<User> users = firestoreService.getAll(USER_COL, User.class);
                        if (users.isEmpty()) {
                                seedUsers();
                        }

                        // Check Departments
                        List<Department> departments = firestoreService.getAll(DEPT_COL, Department.class);
                        if (departments.isEmpty()) {
                                seedHospitalStructure();
                        }

                        System.out.println("--- Firestore Data Ready ---");
                } catch (Exception e) {
                        System.err.println("Error initializing data: " + e.getMessage());
                }
        }

        private void seedUsers() throws ExecutionException, InterruptedException {
                // Patient User
                User patient = User.builder()
                                .firstName("John")
                                .lastName("Doe")
                                .email("test@example.com")
                                .password(passwordEncoder.encode("password123"))
                                .phone("1234567890")
                                .role(UserRole.PATIENT)
                                .build();
                firestoreService.saveWithAutoId(USER_COL, patient);

                // Doctor User
                User doctorUser = User.builder()
                                .firstName("Jane")
                                .lastName("Smith")
                                .email("doctor@example.com")
                                .password(passwordEncoder.encode("doctor123"))
                                .phone("0987654321")
                                .role(UserRole.DOCTOR)
                                .build();
                firestoreService.saveWithAutoId(USER_COL, doctorUser);

                // Admin User
                User admin = User.builder()
                                .firstName("Super")
                                .lastName("Admin")
                                .email("admin@example.com")
                                .password(passwordEncoder.encode("admin123"))
                                .phone("1122334455")
                                .role(UserRole.ADMIN)
                                .build();
                firestoreService.saveWithAutoId(USER_COL, admin);
                System.out.println("Cloud Firestore: Seeded default users.");
        }

        private void seedHospitalStructure() throws ExecutionException, InterruptedException {
                Department cardio = Department.builder().name("Cardiology").code("CARD").build();
                String cardioId = firestoreService.saveWithAutoId(DEPT_COL, cardio);

                Department pedia = Department.builder().name("Pediatrics").code("PED").build();
                String pediaId = firestoreService.saveWithAutoId(DEPT_COL, pedia);

                Department gOpd = Department.builder().name("General OPD").code("GOPD").build();
                String gOpdId = firestoreService.saveWithAutoId(DEPT_COL, gOpd);

                firestoreService.saveWithAutoId(DOC_COL, Doctor.builder()
                                .name("Dr. Smith")
                                .email("doctor@example.com")
                                .specialization("Cardiologist")
                                .roomNumber("R101")
                                .departmentId(cardioId)
                                .build());

                firestoreService.saveWithAutoId(DOC_COL, Doctor.builder()
                                .name("Dr. Johnson")
                                .email("johnson@example.com")
                                .specialization("Pediatrician")
                                .roomNumber("R201")
                                .departmentId(pediaId)
                                .build());

                firestoreService.saveWithAutoId(DOC_COL, Doctor.builder()
                                .name("Dr. Williams")
                                .email("williams@example.com")
                                .specialization("General Physician")
                                .roomNumber("R001")
                                .departmentId(gOpdId)
                                .build());

                System.out.println("Cloud Firestore: Seeded departments and doctors.");
        }
}
