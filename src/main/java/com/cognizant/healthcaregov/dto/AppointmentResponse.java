package com.cognizant.healthcaregov.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private Integer appointmentID;
    private Integer patientID;
    private String patientName;
    private Integer doctorID;
    private String doctorName;
    private Integer hospitalID;
    private String hospitalName;
    private LocalDate date;
    private LocalTime time;
    private String status;
}
