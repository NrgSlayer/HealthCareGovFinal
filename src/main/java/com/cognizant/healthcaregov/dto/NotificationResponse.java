package com.cognizant.healthcaregov.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Integer notificationID;
    private Integer userId;
    private Integer entityId;
    private String message;
    private String category;
    private String status;
    private LocalDateTime createdDate;
}
