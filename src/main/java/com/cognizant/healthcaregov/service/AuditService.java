package com.cognizant.healthcaregov.service;

import com.cognizant.healthcaregov.dao.AuditRepository;
import com.cognizant.healthcaregov.dao.UserRepository;
import com.cognizant.healthcaregov.dto.AuditRequest;
import com.cognizant.healthcaregov.dto.AuditResponse;
import com.cognizant.healthcaregov.entity.Audit;
import com.cognizant.healthcaregov.entity.User;
import com.cognizant.healthcaregov.exception.BadRequestException;
import com.cognizant.healthcaregov.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository auditRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;


    @Transactional
    public AuditResponse create(AuditRequest req) {
        log.info("Creating audit scope={} officerId={}", req.getScope(), req.getOfficerId());
        User officer = findOfficer(req.getOfficerId());

        Audit audit = new Audit();
        audit.setOfficer(officer);
        audit.setScope(req.getScope());
        audit.setFindings(req.getFindings());
        audit.setStatus("Scheduled");

        Audit saved = auditRepository.save(audit);

        log.info("Audit created id={}", saved.getAuditID());
        return toResponse(saved);
    }


    @Transactional
    public AuditResponse update(Integer auditId, AuditRequest req, Integer requesterId) {
        log.info("Updating auditId={} by requesterId={}", auditId, requesterId);
        Audit audit = findById(auditId);

        if ("Completed".equalsIgnoreCase(audit.getStatus())) {
            User requester = userRepository.findById(requesterId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + requesterId));
            boolean isAuditor = requester.getRole().matches("(?i)Auditor|Government_Auditor");
            if (!isAuditor) {
                throw new BadRequestException("Audit is Completed and is read-only for non-auditors.");
            }
        }

        if (req.getStatus() != null && !req.getStatus().isBlank()) {
            validateTransition(audit.getStatus(), req.getStatus());
            audit.setStatus(req.getStatus());
        }
        if (req.getFindings() != null) {
            audit.setFindings(req.getFindings());
        }

        Audit saved = auditRepository.save(audit);

        return toResponse(saved);
    }


    @Transactional(readOnly = true)
    public List<AuditResponse> getAll(String status) {
        log.info("Listing audits status={}", status);
        List<Audit> audits = (status != null && !status.isBlank())
                ? auditRepository.findByStatus(status)
                : auditRepository.findAll();
        return audits.stream().map(this::toResponse).collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public AuditResponse getById(Integer auditId) {
        return toResponse(findById(auditId));
    }

    private void validateTransition(String from, String to) {
        boolean valid = switch (from) {
            case "Scheduled"   -> List.of("Scheduled", "In-Progress").contains(to);
            case "In-Progress" -> List.of("In-Progress", "Completed").contains(to);
            case "Completed"   -> to.equals("Completed");
            default -> false;
        };
        if (!valid) {
            throw new BadRequestException(
                    "Invalid status transition: " + from + " → " + to
                            + ". Allowed flow: Scheduled → In-Progress → Completed");
        }
    }

    private Audit findById(Integer id) {
        return auditRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit not found with id: " + id));
    }

    private User findOfficer(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Officer not found with id: " + id));
    }

    private AuditResponse toResponse(Audit a) {
        return new AuditResponse(a.getAuditID(),
                a.getOfficer().getUserID(), a.getOfficer().getName(),
                a.getScope(), a.getFindings(), a.getDate(), a.getStatus());
    }
}
