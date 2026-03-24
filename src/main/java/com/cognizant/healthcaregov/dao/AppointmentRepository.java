package com.cognizant.healthcaregov.dao;

import com.cognizant.healthcaregov.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    List<Appointment> findByDoctorUserID(Integer doctorId);
    List<Appointment> findByPatientPatientID(Integer patientId);
}
