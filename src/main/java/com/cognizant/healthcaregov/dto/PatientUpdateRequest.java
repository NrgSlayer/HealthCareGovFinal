package com.cognizant.healthcaregov.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PatientUpdateRequest {

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Contact info is required")
    @Pattern(regexp = "\\d{10}", message = "Contact number must be exactly 10 digits")
    private String contactInfo;
}
