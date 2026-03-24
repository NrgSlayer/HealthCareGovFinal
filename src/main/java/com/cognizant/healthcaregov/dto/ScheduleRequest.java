package com.cognizant.healthcaregov.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScheduleRequest {

    @NotNull(message = "Doctor ID is required")
    private Integer doctorId;

    @NotNull(message = "Hospital ID is required")
    private Integer hospitalId;

    @NotBlank(message = "Available date is required (yyyy-MM-dd)")
    private String availableDate;

    @NotBlank(message = "Time slot is required (HH:mm:ss)")
    private String timeSlot;
}
