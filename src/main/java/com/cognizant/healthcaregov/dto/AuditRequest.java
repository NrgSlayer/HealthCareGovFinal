package com.cognizant.healthcaregov.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuditRequest {

    @NotNull(message = "Officer ID is required")
    private Integer officerId;

    @NotBlank(message = "Scope is required")
    private String scope;

    private String findings;

    private String status;
}
