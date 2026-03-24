package com.cognizant.healthcaregov.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {
    private Integer scheduleID;
    private Integer doctorId;
    private String doctorName;
    private Integer hospitalId;
    private String hospitalName;
    private LocalDate availableDate;
    private String timeSlot;
    private String status;
}
