package com.cognizant.healthcaregov.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentReassignRequest {

    @NotNull(message = "New doctor ID is required")
    private Integer newDoctorId;
}
