package com.cognizant.healthcaregov.controller;

import com.cognizant.healthcaregov.dto.AppointmentBookRequest;
import com.cognizant.healthcaregov.dto.AppointmentCancelRequest;
import com.cognizant.healthcaregov.dto.AppointmentReassignRequest;
import com.cognizant.healthcaregov.dto.AppointmentResponse;
import com.cognizant.healthcaregov.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;


    @PostMapping("/book")
    public ResponseEntity<AppointmentResponse> book(
            @Valid @RequestBody AppointmentBookRequest req) {
        log.info("POST /api/appointments/book patientId={}", req.getPatientID());
        return new ResponseEntity<>(appointmentService.book(req), HttpStatus.CREATED);
    }

    @PutMapping("/cancel")
    public ResponseEntity<AppointmentResponse> cancel(
            @Valid @RequestBody AppointmentCancelRequest req,@AuthenticationPrincipal UserDetails userDetails) {
        log.info("PUT /api/appointments/cancel id={}", req.getAppointmentID());
        return ResponseEntity.ok(appointmentService.cancel(req,userDetails));
    }


    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponse>> getByDoctor(
            @PathVariable Integer doctorId) {
        return ResponseEntity.ok(appointmentService.getByDoctor(doctorId));
    }


    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getAll() {
        return ResponseEntity.ok(appointmentService.getAll());
    }


    @PutMapping("/{appointmentId}/reassign")
    public ResponseEntity<AppointmentResponse> reassign(
            @PathVariable Integer appointmentId,
            @Valid @RequestBody AppointmentReassignRequest req,
            @RequestParam Integer adminId) {
        return ResponseEntity.ok(
                appointmentService.reassign(appointmentId, req.getNewDoctorId(), adminId));
    }


    @PutMapping("/{appointmentId}/checkin")
    public ResponseEntity<AppointmentResponse> checkIn(
            @PathVariable Integer appointmentId,
            @RequestParam Integer adminId) {
        log.info("PUT /api/appointments/{}/checkin adminId={}", appointmentId, adminId);
        return ResponseEntity.ok(appointmentService.checkIn(appointmentId, adminId));
    }
}
