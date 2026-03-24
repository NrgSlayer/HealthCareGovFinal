package com.cognizant.healthcaregov.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportRequest {

    @NotNull(message = "Hospital ID is required")
    private Integer hospitalId;

    @NotBlank(message = "Scope is required (Appointment / Treatment / Hospital / Compliance)")
    private String scope;
}
