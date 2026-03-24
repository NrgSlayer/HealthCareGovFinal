package com.cognizant.healthcaregov.controller;

import com.cognizant.healthcaregov.dto.MedicalRecordUpdateRequest;
import com.cognizant.healthcaregov.dto.PatientResponse;
import com.cognizant.healthcaregov.dto.TreatmentRequest;
import com.cognizant.healthcaregov.dto.TreatmentResponse;
import com.cognizant.healthcaregov.service.TreatmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/treatments")
@RequiredArgsConstructor
public class TreatmentController {

    private final TreatmentService treatmentService;
    @PostMapping
    public ResponseEntity<TreatmentResponse> record(
            @Valid @RequestBody TreatmentRequest req) {
        log.info("POST /api/treatments patientId={}", req.getPatientId());
        return new ResponseEntity<>(treatmentService.record(req), HttpStatus.CREATED);
    }
    @PutMapping("/patients/{patientId}")
    public ResponseEntity<PatientResponse> updatePatientRecord(
            @PathVariable Integer patientId,
            @Valid @RequestBody MedicalRecordUpdateRequest req) {
        log.info("PUT /api/treatments/patients/{} updaterId={}", patientId, req.getUpdaterId());
        return ResponseEntity.ok(treatmentService.updatePatientRecord(patientId, req));
    }
}
