package com.cognizant.healthcaregov.controller;

import com.cognizant.healthcaregov.dto.*;
import com.cognizant.healthcaregov.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(userService.getUsers(status));
    }
    @PutMapping("/{userId}/status")
    public ResponseEntity<UserResponse> updateStatus(
            @PathVariable Integer userId,
            @Valid @RequestBody UserStatusRequest req,
            @RequestParam Integer adminId) {
        return ResponseEntity.ok(userService.updateStatus(userId, req, adminId));
    }
}
