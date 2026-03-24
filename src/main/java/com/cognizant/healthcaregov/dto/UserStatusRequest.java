package com.cognizant.healthcaregov.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserStatusRequest {

    @NotBlank(message = "Status is required (Active / Inactive / Rejected)")
    private String status;
}
