package com.cognizant.healthcaregov.controller;

import com.cognizant.healthcaregov.dto.LoginRequest;
import com.cognizant.healthcaregov.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest loginRequestDTO)
    {
        return authService.login(loginRequestDTO);
    }
}

