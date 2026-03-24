package com.cognizant.healthcaregov.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginRequest {

    @NotNull(message="Email is must")
    @Email(message="Enter correct email")
    private String email;

    @NotNull(message="Password is must")
    @Size(min=8,message="Password must be at least 8 characters long")
    private String password;
}

