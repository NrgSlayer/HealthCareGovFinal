package com.cognizant.healthcaregov.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class HospitalRequest {

    @NotBlank(message = "Hospital name is required")
    private String name;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 10000, message = "Capacity cannot exceed 10000")
    private Integer capacity;

    @NotBlank(message = "Status is required")
    private String status;
}
