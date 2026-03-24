package com.cognizant.healthcaregov.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DocumentUploadRequest {

    @NotNull(message = "Patient ID is required")
    private Integer patientID;

    @NotBlank(message = "Document type is required (IDProof / HealthCard)")
    private String docType;

    @NotBlank(message = "File URI is required")
    private String fileURI;
}
