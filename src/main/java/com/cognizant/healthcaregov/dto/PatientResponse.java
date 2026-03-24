package com.cognizant.healthcaregov.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponse {
    private Integer patientID;
    private Integer userID;
    private String name;
    private LocalDate dob;
    private String gender;
    private String address;
    private String contactInfo;
    private String status;
}
