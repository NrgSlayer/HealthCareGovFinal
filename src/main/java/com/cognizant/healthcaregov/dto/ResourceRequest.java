package com.cognizant.healthcaregov.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ResourceRequest {

    @NotNull(message = "Hospital ID is required")
    private Integer hospitalID;

    @NotBlank(message = "Resource type is required (Beds / Equipment / Staff)")
    private String type;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 100000, message = "Quantity cannot exceed 100000")
    private Integer quantity;

    @NotBlank(message = "Status is required")
    private String status;
}
