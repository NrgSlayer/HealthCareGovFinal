package com.cognizant.healthcaregov.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MedicalRecordUpdateRequest {

    @NotNull(message = "Updater ID is required")
    private Integer updaterId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Contact info is required")
    private String contactInfo;

    @NotBlank(message = "Status is required")
    private String status;
}
