package com.cognizant.healthcaregov.service;

import com.cognizant.healthcaregov.dao.ComplianceRecordRepository;
import com.cognizant.healthcaregov.dto.AuditLogResponse;
import com.cognizant.healthcaregov.dto.ComplianceRequest;
import com.cognizant.healthcaregov.dto.ComplianceResponse;
import com.cognizant.healthcaregov.entity.ComplianceRecord;
import com.cognizant.healthcaregov.exception.BadRequestException;
import com.cognizant.healthcaregov.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComplianceService {

    private static final Set<String> VALID_TYPES = Set.of("Appointment", "Treatment", "Hospital");

    private final ComplianceRecordRepository complianceRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;


    @Transactional
    public ComplianceResponse create(ComplianceRequest req, Integer officerId) {
        log.info("Creating compliance record type={} entityId={}", req.getType(), req.getEntityId());
        if (!VALID_TYPES.contains(req.getType())) {
            throw new BadRequestException("Invalid type. Allowed: Appointment, Treatment, Hospital");
        }

        ComplianceRecord record = new ComplianceRecord();
        record.setEntityId(req.getEntityId());
        record.setType(req.getType());
        record.setResult(req.getResult());
        record.setNotes(req.getNotes());

        ComplianceRecord saved = complianceRepository.save(record);

        log.info("Compliance record saved id={}", saved.getComplianceID());


        notificationService.send(officerId, saved.getComplianceID(),
                "Compliance record created for " + saved.getType()
                        + " (EntityID: " + saved.getEntityId() + "). Review for upcoming deadlines.",
                "Compliance");

        return toResponse(saved);
    }


    @Transactional
    public ComplianceResponse update(Integer id, ComplianceRequest req, Integer officerId) {
        log.info("Updating complianceId={}", id);
        ComplianceRecord record = findById(id);
        record.setResult(req.getResult());
        record.setNotes(req.getNotes());
        ComplianceRecord saved = complianceRepository.save(record);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ComplianceResponse> search(String type, String result, Integer entityId,
                                           LocalDate startDate, LocalDate endDate) {
        log.info("Searching compliance records type={} result={} entityId={}", type, result, entityId);
        return complianceRepository.search(type, result, entityId, startDate, endDate)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogs() {
        log.info("Fetching all audit logs");
        return auditLogService.getAllLogs();
    }


    private ComplianceRecord findById(Integer id) {
        return complianceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Compliance record not found with id: " + id));
    }

    private ComplianceResponse toResponse(ComplianceRecord c) {
        return new ComplianceResponse(c.getComplianceID(), c.getEntityId(),
                c.getType(), c.getResult(), c.getDate(), c.getNotes());
    }
}
