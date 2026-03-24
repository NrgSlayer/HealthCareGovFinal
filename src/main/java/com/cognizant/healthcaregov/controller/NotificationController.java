package com.cognizant.healthcaregov.controller;

import com.cognizant.healthcaregov.dto.NotificationResponse;
import com.cognizant.healthcaregov.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;


    @GetMapping("/{userId}")
    public ResponseEntity<List<NotificationResponse>> getForUser(
            @PathVariable Integer userId) {
        log.info("GET /api/notifications/{}", userId);
        return ResponseEntity.ok(notificationService.getForUser(userId));
    }
}
