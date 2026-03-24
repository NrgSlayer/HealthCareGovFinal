package com.cognizant.healthcaregov.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {
    private long totalHospitals;
    private long totalBeds;
    private long totalEquipment;
    private long totalStaff;
    private long totalCapacity;
}
