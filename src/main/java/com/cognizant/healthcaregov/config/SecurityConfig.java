package com.cognizant.healthcaregov.config;

import com.cognizant.healthcaregov.security.JwtAccessDeniedHandler;
import com.cognizant.healthcaregov.security.JwtAuthenticationEntryPoint;
import com.cognizant.healthcaregov.filter.JwtAuthenticationFilter;
//import com.cognizant.healthcaregov.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf->csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        // --- PUBLIC ---
                        .requestMatchers(HttpMethod.POST, "/api/users/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/patients/register").permitAll()

                        // --- USER MANAGEMENT ---
                        .requestMatchers(HttpMethod.GET,  "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,  "/api/users/*/status").hasRole("ADMIN")

                        // --- PATIENT PROFILE ---
                        .requestMatchers(HttpMethod.GET,  "/api/patients/*/history").hasAnyRole("PATIENT","DOCTOR","ADMIN")
                        .requestMatchers(HttpMethod.GET,  "/api/patients/*/summary").hasAnyRole("PATIENT","DOCTOR","ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/patients/documents").hasAnyRole("PATIENT","ADMIN")
                        .requestMatchers(HttpMethod.GET,  "/api/patients/*").hasAnyRole("PATIENT","DOCTOR","ADMIN")
                        .requestMatchers(HttpMethod.PUT,  "/api/patients/*").hasAnyRole("PATIENT","ADMIN")

                        // --- HOSPITAL MANAGEMENT ---
                        .requestMatchers(HttpMethod.POST,   "/api/hospitals").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/hospitals/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/hospitals/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/hospitals/**").authenticated()

                        // --- RESOURCE MANAGEMENT ---
                        .requestMatchers(HttpMethod.POST,   "/api/resources").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/resources/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/resources/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/resources/**").authenticated()

                        // --- ANALYTICS ---
                        .requestMatchers("/api/analytics/**").hasAnyRole("ADMIN","PROGRAM_MANAGER")

                        // --- SCHEDULES ---
                        .requestMatchers(HttpMethod.POST, "/api/schedules").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.PUT,  "/api/schedules/*").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.GET,  "/api/schedules/**").authenticated()

                        // --- APPOINTMENTS ---
                        .requestMatchers(HttpMethod.POST, "/api/appointments/book").hasRole("PATIENT")
                        .requestMatchers(HttpMethod.PUT,  "/api/appointments/cancel").hasRole("PATIENT")
                        .requestMatchers(HttpMethod.GET,  "/api/appointments/doctor/*").hasAnyRole("DOCTOR","ADMIN")
                        .requestMatchers(HttpMethod.GET,  "/api/appointments").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,  "/api/appointments/*/reassign").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,  "/api/appointments/*/checkin").hasRole("ADMIN")

                        // --- TREATMENTS ---
                        .requestMatchers(HttpMethod.POST, "/api/treatments").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.PUT,  "/api/treatments/patients/*").hasAnyRole("DOCTOR","ADMIN")

                        // --- COMPLIANCE ---
                        .requestMatchers(HttpMethod.POST, "/api/compliance").hasRole("COMP_OFFICER")
                        .requestMatchers(HttpMethod.PUT,  "/api/compliance/*").hasRole("COMP_OFFICER")
                        .requestMatchers(HttpMethod.GET,  "/api/compliance/audit-logs").hasAnyRole("COMP_OFFICER","ADMIN","AUDITOR")
                        .requestMatchers(HttpMethod.GET,  "/api/compliance").hasAnyRole("COMP_OFFICER","ADMIN")

                        // --- AUDITS ---
                        .requestMatchers(HttpMethod.POST, "/api/audits").hasRole("AUDITOR")
                        .requestMatchers(HttpMethod.PUT,  "/api/audits/*").hasRole("AUDITOR")
                        .requestMatchers(HttpMethod.GET,  "/api/audits/**").hasAnyRole("AUDITOR","ADMIN")

                        // --- NOTIFICATIONS ---
                        .requestMatchers("/api/notifications/**").authenticated()

                        // --- PROGRAM ---
                        .requestMatchers("/api/program/**").hasAnyRole("ADMIN","PROGRAM_MANAGER")

                        .anyRequest().denyAll()
                )
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
