package com.cognizant.healthcaregov.controller;

import com.cognizant.healthcaregov.dto.*;
import com.cognizant.healthcaregov.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @PostMapping("/register")
    public ResponseEntity<PatientResponse> register(
            @Valid @RequestBody PatientRegisterRequest req) {
        log.info("POST /api/patients/register");
        return new ResponseEntity<>(patientService.register(req), HttpStatus.CREATED);
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<PatientResponse> getProfile(@PathVariable Integer patientId) {
        return ResponseEntity.ok(patientService.getProfile(patientId));
    }

    @PutMapping("/{patientId}")
    public ResponseEntity<PatientResponse> updateProfile(
            @PathVariable Integer patientId,
            @Valid @RequestBody PatientUpdateRequest req) {
        return ResponseEntity.ok(patientService.updateProfile(patientId, req));
    }

    @PostMapping("/documents")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @Valid @RequestBody DocumentUploadRequest req) {
        log.info("POST /api/patients/documents patientId={}", req.getPatientID());
        return new ResponseEntity<>(patientService.uploadDocument(req), HttpStatus.CREATED);
    }

    @GetMapping("/{patientId}/history")
    public ResponseEntity<List<TreatmentResponse>> getMedicalHistory(
            @PathVariable Integer patientId) {
        return ResponseEntity.ok(patientService.getMedicalHistory(patientId));
    }

    @GetMapping("/{patientId}/summary")
    public ResponseEntity<MedicalSummaryResponse> getMedicalSummary(
            @PathVariable Integer patientId) {
        return ResponseEntity.ok(patientService.getMedicalSummary(patientId));
    }
}