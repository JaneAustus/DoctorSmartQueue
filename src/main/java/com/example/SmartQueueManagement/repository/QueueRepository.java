package com.example.SmartQueueManagement.repository;

import com.example.SmartQueueManagement.model.QueueEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QueueRepository extends JpaRepository<QueueEntry, Long> {
    List<QueueEntry> findByPatientIdIgnoreCase(String patientId);

    List<QueueEntry> findByDoctorId(String doctorId);
}
