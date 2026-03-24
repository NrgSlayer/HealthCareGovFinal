package com.cognizant.healthcaregov.controller;

import com.cognizant.healthcaregov.dto.ScheduleRequest;
import com.cognizant.healthcaregov.dto.ScheduleResponse;
import com.cognizant.healthcaregov.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;


    @PostMapping
    public ResponseEntity<ScheduleResponse> create(@Valid @RequestBody ScheduleRequest req) {
        log.info("POST /api/schedules doctorId={}", req.getDoctorId());
        return new ResponseEntity<>(scheduleService.create(req), HttpStatus.CREATED);
    }


    @PutMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponse> update(
            @PathVariable Integer scheduleId,
            @Valid @RequestBody ScheduleRequest req) {
        return ResponseEntity.ok(scheduleService.update(scheduleId, req));
    }


    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<ScheduleResponse>> getByDoctor(@PathVariable Integer doctorId) {
        return ResponseEntity.ok(scheduleService.getByDoctor(doctorId));
    }
}
