package com.cognizant.healthcaregov.dao;

import com.cognizant.healthcaregov.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {
    Optional<Patient> findByUserUserID(Integer userId);
}
