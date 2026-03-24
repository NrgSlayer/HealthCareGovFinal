package com.cognizant.healthcaregov.service;

import com.cognizant.healthcaregov.dao.AuditLogRepository;
import com.cognizant.healthcaregov.dao.UserRepository;
import com.cognizant.healthcaregov.dto.AuditLogResponse;
import com.cognizant.healthcaregov.entity.AuditLog;
import com.cognizant.healthcaregov.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;


    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAllLogs() {
        return auditLogRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private AuditLogResponse toResponse(AuditLog l) {
        User u = l.getUser();
        return new AuditLogResponse(
                l.getAuditLogID(),
                u != null ? u.getUserID() : null,
                u != null ? u.getName() : null,
                l.getAction(),
                l.getResource(),
                l.getTimestamp()
        );
    }
}
