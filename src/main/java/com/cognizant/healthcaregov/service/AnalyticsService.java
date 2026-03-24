package com.cognizant.healthcaregov.service;

import com.cognizant.healthcaregov.dao.HospitalRepository;
import com.cognizant.healthcaregov.dto.AnalyticsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final HospitalRepository hospitalRepository;
    private final ResourceService resourceService;


    @Transactional(readOnly = true)
    public AnalyticsResponse getHospitalAnalytics() {
        log.info("Building hospital analytics dashboard");
        long totalHospitals = hospitalRepository.count();
        long totalCapacity  = hospitalRepository.findAll().stream()
                .mapToLong(h -> h.getCapacity() != null ? h.getCapacity() : 0L).sum();
        long totalBeds      = resourceService.sumByType("Beds");
        long totalEquipment = resourceService.sumByType("Equipment");
        long totalStaff     = resourceService.sumByType("Staff");
        return new AnalyticsResponse(totalHospitals, totalBeds, totalEquipment, totalStaff, totalCapacity);
    }


    @Transactional(readOnly = true)
    public Map<String, Object> getCapacityReport() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalCapacity", hospitalRepository.findAll().stream()
                .mapToLong(h -> h.getCapacity() != null ? h.getCapacity() : 0L).sum());
        return report;
    }


    @Transactional(readOnly = true)
    public Map<String, Object> getResourceAvailabilityReport() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("bedsAvailable",      resourceService.sumByType("Beds"));
        report.put("equipmentAvailable", resourceService.sumByType("Equipment"));
        report.put("staffAvailable",     resourceService.sumByType("Staff"));
        return report;
    }


    @Transactional(readOnly = true)
    public Map<String, Object> getResourceDistributionReport() {
        return getResourceAvailabilityReport();
    }
}
