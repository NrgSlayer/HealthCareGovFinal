package com.cognizant.healthcaregov.dao;

import com.cognizant.healthcaregov.entity.PatientDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientDocumentRepository extends JpaRepository<PatientDocument, Integer> {
    List<PatientDocument> findByPatientPatientID(Integer patientId);
}
