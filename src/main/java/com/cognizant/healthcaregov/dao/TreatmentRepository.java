package com.cognizant.healthcaregov.dao;

import com.cognizant.healthcaregov.entity.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TreatmentRepository extends JpaRepository<Treatment, Integer> {
    List<Treatment> findByPatientPatientID(Integer patientId);
}
