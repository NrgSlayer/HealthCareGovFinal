package com.cognizant.healthcaregov.controller;

import com.cognizant.healthcaregov.dto.AnalyticsResponse;
import com.cognizant.healthcaregov.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;


    @GetMapping("/hospitals")
    public ResponseEntity<AnalyticsResponse> getHospitalAnalytics() {
        log.info("GET /api/analytics/hospitals");
        return ResponseEntity.ok(analyticsService.getHospitalAnalytics());
    }


    @GetMapping("/reports/hospital-capacity")
    public ResponseEntity<Map<String, Object>> getCapacityReport() {
        return ResponseEntity.ok(analyticsService.getCapacityReport());
    }


    @GetMapping("/reports/resource-availability")
    public ResponseEntity<Map<String, Object>> getAvailabilityReport() {
        return ResponseEntity.ok(analyticsService.getResourceAvailabilityReport());
    }


    @GetMapping("/reports/resource-distribution")
    public ResponseEntity<Map<String, Object>> getDistributionReport() {
        return ResponseEntity.ok(analyticsService.getResourceDistributionReport());
    }
}
