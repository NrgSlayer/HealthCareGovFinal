package com.cognizant.healthcaregov.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ComplianceRequest {

    @NotNull(message = "Entity ID is required")
    private Integer entityId;

    @NotBlank(message = "Type is required (Appointment / Treatment / Hospital)")
    private String type;

    @NotBlank(message = "Result is required (Pass / Fail)")
    private String result;

    private String notes;
}
