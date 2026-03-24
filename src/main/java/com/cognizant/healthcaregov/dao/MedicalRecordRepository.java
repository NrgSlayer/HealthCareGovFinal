package com.cognizant.healthcaregov.dao;

import com.cognizant.healthcaregov.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Integer> {
    Optional<MedicalRecord> findByPatientPatientID(Integer patientId);
}
