package com.cognizant.healthcaregov.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Integer auditLogID;
    private Integer userId;
    private String userName;
    private String action;
    private String resource;
    private Instant timestamp;
}
