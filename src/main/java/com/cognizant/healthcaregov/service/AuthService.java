package com.cognizant.healthcaregov.service;

import com.cognizant.healthcaregov.dto.LoginRequest;
import com.cognizant.healthcaregov.util.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AuthService {

    private AuthenticationManager authenticationManager;

    private JwtUtil jwtUtil;

    public String login(LoginRequest loginRequestDTO)
    {
        log.info("User with email:{} tried to login",loginRequestDTO.getEmail());
        Authentication authentication=authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(),loginRequestDTO.getPassword()));
        UserDetails userDetails=(UserDetails) authentication.getPrincipal();
        String token=jwtUtil.generateToken(userDetails);
        log.info("Login Successfull for User: {}",userDetails.getUsername());
        return token;
    }
}
