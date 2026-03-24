package com.cognizant.healthcaregov.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TreatmentResponse {
    private Integer treatmentID;
    private Integer patientID;
    private String patientName;
    private Integer doctorID;
    private String doctorName;
    private String diagnosis;
    private String prescription;
    private String treatmentNotes;
    private LocalDate date;
    private String status;
}
