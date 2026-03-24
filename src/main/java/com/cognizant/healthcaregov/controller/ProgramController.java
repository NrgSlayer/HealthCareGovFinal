package com.cognizant.healthcaregov.controller;

import com.cognizant.healthcaregov.dto.DashboardResponse;
import com.cognizant.healthcaregov.dto.ReportRequest;
import com.cognizant.healthcaregov.dto.ReportResponse;
import com.cognizant.healthcaregov.service.ProgramService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/program")
@RequiredArgsConstructor
public class ProgramController {

    private final ProgramService programService;


    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /api/program/dashboard startDate={} endDate={}", startDate, endDate);
        return ResponseEntity.ok(programService.getDashboard(startDate, endDate));
    }


    @PostMapping("/reports")
    public ResponseEntity<ReportResponse> generateReport(
            @Valid @RequestBody ReportRequest req) {
        log.info("POST /api/program/reports scope={}", req.getScope());
        return new ResponseEntity<>(programService.generateReport(req), HttpStatus.CREATED);
    }
}
