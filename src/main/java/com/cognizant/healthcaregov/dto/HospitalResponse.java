package com.cognizant.healthcaregov.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HospitalResponse {
    private Integer hospitalID;
    private String name;
    private String location;
    private Integer capacity;
    private String status;
}
