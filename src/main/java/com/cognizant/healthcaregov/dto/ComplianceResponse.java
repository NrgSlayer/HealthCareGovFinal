package com.cognizant.healthcaregov.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceResponse {
    private Integer complianceID;
    private Integer entityId;
    private String type;
    private String result;
    private LocalDate date;
    private String notes;
}
