package com.cognizant.healthcaregov.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private Integer reportID;
    private Integer hospitalId;
    private String hospitalName;
    private String scope;
    private String metrics;
    private LocalDateTime generatedDate;
}
