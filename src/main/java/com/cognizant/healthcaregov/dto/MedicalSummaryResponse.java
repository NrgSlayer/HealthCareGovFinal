package com.cognizant.healthcaregov.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalSummaryResponse {
    private Integer recordID;
    private Integer patientID;
    private String patientName;
    private String contactInfo;
    private String detailsJSON;
    private LocalDate date;
    private String status;
}
