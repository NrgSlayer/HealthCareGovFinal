package com.cognizant.healthcaregov.service;

import com.cognizant.healthcaregov.dao.MedicalRecordRepository;
import com.cognizant.healthcaregov.dao.PatientDocumentRepository;
import com.cognizant.healthcaregov.dao.PatientRepository;
import com.cognizant.healthcaregov.dao.TreatmentRepository;
import com.cognizant.healthcaregov.dao.MedicalRecordRepository;
import com.cognizant.healthcaregov.dao.UserRepository;
import com.cognizant.healthcaregov.dto.*;
import com.cognizant.healthcaregov.entity.Patient;
import com.cognizant.healthcaregov.entity.PatientDocument;
import com.cognizant.healthcaregov.entity.User;
import com.cognizant.healthcaregov.exception.BadRequestException;
import com.cognizant.healthcaregov.exception.ResourceNotFoundException;
import com.cognizant.healthcaregov.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientService {

    private static final Set<String> ALLOWED_DOC_TYPES = Set.of("IDProof", "HealthCard");

    private final PatientRepository        patientRepository;
    private final PatientDocumentRepository documentRepository;
    private final TreatmentRepository      treatmentRepository;
    private final MedicalRecordRepository  medicalRecordRepository;
    private final UserRepository           userRepository;
    private final PasswordEncoder          passwordEncoder;
    private final SecurityUtils            securityUtils;

    @Transactional
    public PatientResponse register(PatientRegisterRequest req) {
        log.info("Registering patient email={}", req.getEmail());

        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new BadRequestException("Email is already registered: " + req.getEmail());
        }
        User user = new User();
        user.setName(req.getName());
        user.setRole("Patient");
        user.setEmail(req.getEmail());
        user.setPhone(req.getContactInfo());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setStatus("Pending");
        User savedUser = userRepository.save(user);
        Patient patient = new Patient();
        patient.setUser(savedUser);
        patient.setName(req.getName());
        patient.setDob(LocalDate.parse(req.getDob()));
        patient.setGender(req.getGender());
        patient.setAddress(req.getAddress());
        patient.setContactInfo(req.getContactInfo());
        patient.setStatus("Pending");
        Patient saved = patientRepository.save(patient);
        log.info("Patient registered patientID={} userID={} status=Pending",
                saved.getPatientID(), savedUser.getUserID());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PatientResponse getProfile(Integer patientId) {
        log.info("Fetching profile patientId={}", patientId);
        Patient patient = findById(patientId);
        verifyPatientOwnership(patient, "view profile");
        return toResponse(patient);
    }

    @Transactional
    public PatientResponse updateProfile(Integer patientId, PatientUpdateRequest req) {
        log.info("Updating profile patientId={}", patientId);
        Patient patient = findById(patientId);
        verifyPatientOwnership(patient, "update profile");
        patient.setAddress(req.getAddress());
        patient.setContactInfo(req.getContactInfo());
        return toResponse(patientRepository.save(patient));
    }

    @Transactional
    public DocumentResponse uploadDocument(DocumentUploadRequest req) {
        log.info("Uploading document for patientId={}", req.getPatientID());
        if (!ALLOWED_DOC_TYPES.contains(req.getDocType())) {
            throw new BadRequestException("Invalid docType. Allowed: IDProof, HealthCard");
        }
        Patient patient = findById(req.getPatientID());
        verifyPatientOwnership(patient, "upload document");

        PatientDocument doc = new PatientDocument();
        doc.setPatient(patient);
        doc.setDocType(req.getDocType());
        doc.setFileURI(req.getFileURI());
        doc.setVerificationStatus("Pending");
        PatientDocument saved = documentRepository.save(doc);
        log.info("Document saved documentID={}", saved.getDocumentID());
        return new DocumentResponse(saved.getDocumentID(), patient.getPatientID(),
                saved.getDocType(), saved.getFileURI(),
                saved.getUploadedDate(), saved.getVerificationStatus());
    }

    @Transactional(readOnly = true)
    public List<TreatmentResponse> getMedicalHistory(Integer patientId) {
        log.info("Fetching treatment history patientId={}", patientId);
        Patient patient = findById(patientId);
        verifyPatientOwnership(patient, "view medical history");

        return treatmentRepository.findByPatientPatientID(patientId).stream()
                .map(t -> new TreatmentResponse(
                        t.getTreatmentID(),
                        t.getPatient().getPatientID(), t.getPatient().getName(),
                        t.getDoctor().getUserID(), t.getDoctor().getName(),
                        t.getDiagnosis(), t.getPrescription(), t.getTreatmentNotes(),
                        t.getDate(), t.getStatus()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MedicalSummaryResponse getMedicalSummary(Integer patientId) {
        log.info("Fetching medical summary patientId={}", patientId);
        Patient patient = findById(patientId);
        verifyPatientOwnership(patient, "view medical summary");

        return medicalRecordRepository.findByPatientPatientID(patientId)
                .map(r -> new MedicalSummaryResponse(
                        r.getRecordID(),
                        r.getPatient().getPatientID(), r.getPatient().getName(),
                        r.getPatient().getContactInfo(),
                        r.getDetailsJSON(), r.getDate(), r.getStatus()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No medical record found for patientId: " + patientId));
    }

    private void verifyPatientOwnership(Patient patient, String action) {
        if (securityUtils.isAdmin() || securityUtils.hasRole("DOCTOR")) {
            return;
        }
        String callerEmail = securityUtils.getCurrentUserEmail().orElse(null);
        User linkedUser = patient.getUser();
        if (linkedUser == null || !linkedUser.getEmail().equalsIgnoreCase(callerEmail)) {
            log.warn("Access denied: Patient caller {} attempted to {} for patientId={}",
                    callerEmail, action, patient.getPatientID());
            throw new BadRequestException(
                    "Access denied: you may only " + action + " for your own account.");
        }
    }

    private Patient findById(Integer id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + id));
    }

    private PatientResponse toResponse(Patient p) {
        Integer userId = (p.getUser() != null) ? p.getUser().getUserID() : null;
        return new PatientResponse(
                p.getPatientID(), userId,
                p.getName(), p.getDob(), p.getGender(),
                p.getAddress(), p.getContactInfo(), p.getStatus());
    }
}