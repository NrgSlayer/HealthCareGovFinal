package com.cognizant.healthcaregov.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceResponse {
    private Integer resourceID;
    private Integer hospitalID;
    private String hospitalName;
    private String type;
    private Integer quantity;
    private String status;
}
