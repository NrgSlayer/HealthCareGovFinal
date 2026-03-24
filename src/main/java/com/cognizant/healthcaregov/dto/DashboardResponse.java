package com.cognizant.healthcaregov.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private long totalAppointments;
    private long confirmedAppointments;
    private long cancelledAppointments;
    private long totalTreatments;
    private long activeTreatments;
    private long completedTreatments;
    private long totalHospitals;
    private long totalCapacity;
    private long totalComplianceRecords;
    private long passedCompliance;
    private long failedCompliance;
}
