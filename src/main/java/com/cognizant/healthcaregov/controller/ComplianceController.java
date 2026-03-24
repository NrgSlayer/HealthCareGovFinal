package com.cognizant.healthcaregov.controller;

import com.cognizant.healthcaregov.dto.AuditLogResponse;
import com.cognizant.healthcaregov.dto.ComplianceRequest;
import com.cognizant.healthcaregov.dto.ComplianceResponse;
import com.cognizant.healthcaregov.service.ComplianceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/compliance")
@RequiredArgsConstructor
public class ComplianceController {

    private final ComplianceService complianceService;

    @PostMapping
    public ResponseEntity<ComplianceResponse> create(
            @Valid @RequestBody ComplianceRequest req,
            @RequestParam Integer officerId) {
        log.info("POST /api/compliance officerId={}", officerId);
        return new ResponseEntity<>(complianceService.create(req, officerId), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ComplianceResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody ComplianceRequest req,
            @RequestParam Integer officerId) {
        return ResponseEntity.ok(complianceService.update(id, req, officerId));
    }


    @GetMapping
    public ResponseEntity<List<ComplianceResponse>> search(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) Integer entityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(complianceService.search(type, result, entityId, startDate, endDate));
    }


    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs() {
        log.info("GET /api/compliance/audit-logs");
        return ResponseEntity.ok(complianceService.getAuditLogs());
    }
}
