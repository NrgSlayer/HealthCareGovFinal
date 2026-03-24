package com.cognizant.healthcaregov.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditResponse {
    private Integer auditID;
    private Integer officerId;
    private String officerName;
    private String scope;
    private String findings;
    private LocalDate date;
    private String status;
}
