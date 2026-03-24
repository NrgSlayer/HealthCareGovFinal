package com.cognizant.healthcaregov.service;

import com.cognizant.healthcaregov.dao.PatientRepository;
import com.cognizant.healthcaregov.dao.UserRepository;
import com.cognizant.healthcaregov.dto.*;
import com.cognizant.healthcaregov.entity.User;
import com.cognizant.healthcaregov.exception.BadRequestException;
import com.cognizant.healthcaregov.exception.ResourceNotFoundException;
import com.cognizant.healthcaregov.util.JwtUtil;
//import com.cognizant.healthcaregov.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final Set<String> VALID_STATUSES = Set.of("Active", "Inactive", "Rejected");
    private static final Set<String> VALID_ROLES    =
            Set.of("Admin", "Doctor", "Compliance", "Auditor", "Manager");
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtils;
//    private final SecurityUtils securityUtils;


    @Transactional(readOnly = true)
    public List<UserResponse> getUsers(String status) {
        log.info("Fetching users status={}", status);
        List<User> users = (status != null && !status.isBlank())
                ? userRepository.findByStatus(status)
                : userRepository.findAll();
        return users.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateStatus(Integer userId, UserStatusRequest req, Integer adminId) {
        log.info("Admin {} updating status of userId={} to {}", adminId, userId, req.getStatus());
        if (!VALID_STATUSES.contains(req.getStatus())) {
            throw new BadRequestException("Invalid status. Allowed: Active, Inactive, Rejected");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));
        if(user.getRole().equals("PATIENT")) {
            user.setStatus(req.getStatus());
        }
        User saved = userRepository.save(user);
        return toResponse(saved);
    }
    private UserResponse toResponse(User u) {
        return new UserResponse(u.getUserID(), u.getName(), u.getRole(),
                u.getEmail(), u.getPhone(), u.getStatus());
    }
}

