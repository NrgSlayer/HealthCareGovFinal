package com.cognizant.healthcaregov.controller;

import com.cognizant.healthcaregov.dto.AuditRequest;
import com.cognizant.healthcaregov.dto.AuditResponse;
import com.cognizant.healthcaregov.service.AuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/audits")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;


    @PostMapping
    public ResponseEntity<AuditResponse> create(@Valid @RequestBody AuditRequest req) {
        log.info("POST /api/audits officerId={}", req.getOfficerId());
        return new ResponseEntity<>(auditService.create(req), HttpStatus.CREATED);
    }


    @PutMapping("/{auditId}")
    public ResponseEntity<AuditResponse> update(
            @PathVariable Integer auditId,
            @Valid @RequestBody AuditRequest req,
            @RequestParam Integer requesterId) {
        return ResponseEntity.ok(auditService.update(auditId, req, requesterId));
    }


    @GetMapping
    public ResponseEntity<List<AuditResponse>> getAll(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(auditService.getAll(status));
    }


    @GetMapping("/{auditId}")
    public ResponseEntity<AuditResponse> getById(@PathVariable Integer auditId) {
        return ResponseEntity.ok(auditService.getById(auditId));
    }
}
