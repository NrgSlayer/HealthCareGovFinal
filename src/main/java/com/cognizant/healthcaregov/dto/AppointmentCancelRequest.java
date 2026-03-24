package com.cognizant.healthcaregov.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentCancelRequest {

    @NotNull(message = "Appointment ID is required")
    @Min(value = 1, message = "Appointment ID must be positive")
    private Integer appointmentID;
}
