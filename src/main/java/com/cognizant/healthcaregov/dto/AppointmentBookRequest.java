package com.cognizant.healthcaregov.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AppointmentBookRequest {

    @NotNull(message = "Patient ID is required")
    @Min(value = 1, message = "Patient ID must be positive")
    private Integer patientID;

    @NotNull(message = "Doctor ID is required")
    @Min(value = 1, message = "Doctor ID must be positive")
    private Integer doctorID;

    @NotNull(message = "Hospital ID is required")
    @Min(value = 1, message = "Hospital ID must be positive")
    private Integer hospitalID;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Time is required")
    private LocalTime time;
}
