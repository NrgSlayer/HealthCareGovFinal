package com.cognizant.healthcaregov.service;

import com.cognizant.healthcaregov.dao.MedicalRecordRepository;
import com.cognizant.healthcaregov.dao.PatientRepository;
import com.cognizant.healthcaregov.dao.TreatmentRepository;
import com.cognizant.healthcaregov.dao.UserRepository;
import com.cognizant.healthcaregov.dto.MedicalRecordUpdateRequest;
import com.cognizant.healthcaregov.dto.PatientResponse;
import com.cognizant.healthcaregov.dto.TreatmentRequest;
import com.cognizant.healthcaregov.dto.TreatmentResponse;
import com.cognizant.healthcaregov.entity.MedicalRecord;
import com.cognizant.healthcaregov.entity.Patient;
import com.cognizant.healthcaregov.entity.Treatment;
import com.cognizant.healthcaregov.entity.User;
import com.cognizant.healthcaregov.exception.BadRequestException;
import com.cognizant.healthcaregov.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TreatmentService {

    private final TreatmentRepository treatmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public TreatmentResponse record(TreatmentRequest req) {
        log.info("Recording treatment patientId={} doctorId={}", req.getPatientId(), req.getDoctorId());

        Patient patient = patientRepository.findById(req.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + req.getPatientId()));
        User doctor = userRepository.findById(req.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with id: " + req.getDoctorId()));

        Treatment t = new Treatment();
        t.setPatient(patient);
        t.setDoctor(doctor);
        t.setDiagnosis(req.getDiagnosis());
        t.setPrescription(req.getPrescription());
        t.setTreatmentNotes(req.getTreatmentNotes());
        t.setStatus(req.getStatus());
        Treatment saved = treatmentRepository.save(t);
        log.info("Treatment saved treatmentID={}", saved.getTreatmentID());

        MedicalRecord record = medicalRecordRepository
                .findByPatientPatientID(patient.getPatientID())
                .orElse(new MedicalRecord());
        record.setPatient(patient);
        String existing = record.getDetailsJSON() != null ? record.getDetailsJSON() : "";
        record.setDetailsJSON(existing + " | Diagnosis: " + saved.getDiagnosis());
        record.setStatus(saved.getStatus());
        medicalRecordRepository.save(record);

        notificationService.send(doctor.getUserID(), saved.getTreatmentID(),
                "Treatment recorded for patient " + patient.getName()
                        + " — Diagnosis: " + saved.getDiagnosis(),
                "Treatment");

        return toTreatmentResponse(saved);
    }

    @Transactional
    public PatientResponse updatePatientRecord(Integer patientId, MedicalRecordUpdateRequest req) {
        log.info("Updating patient record patientId={} updaterId={}", patientId, req.getUpdaterId());

        User updater = userRepository.findById(req.getUpdaterId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + req.getUpdaterId()));
        if (!updater.getRole().matches("(?i)Doctor|Admin")) {
            throw new BadRequestException("Access denied. Only Doctors and Admins can update patient records.");
        }

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + patientId));
        if ("Finalized".equalsIgnoreCase(patient.getStatus())) {
            throw new BadRequestException("Cannot update a Finalized patient record.");
        }

        patient.setName(req.getName());
        patient.setContactInfo(req.getContactInfo());
        patient.setStatus(req.getStatus());
        Patient saved = patientRepository.save(patient);
        log.info("Patient record updated patientId={}", patientId);

        Integer linkedUserId = (saved.getUser() != null) ? saved.getUser().getUserID() : null;
        return new PatientResponse(
                saved.getPatientID(),
                linkedUserId,
                saved.getName(),
                saved.getDob(),
                saved.getGender(),
                saved.getAddress(),
                saved.getContactInfo(),
                saved.getStatus());
    }

    private TreatmentResponse toTreatmentResponse(Treatment t) {
        return new TreatmentResponse(
                t.getTreatmentID(),
                t.getPatient().getPatientID(), t.getPatient().getName(),
                t.getDoctor().getUserID(), t.getDoctor().getName(),
                t.getDiagnosis(), t.getPrescription(), t.getTreatmentNotes(),
                t.getDate(), t.getStatus());
    }
}
