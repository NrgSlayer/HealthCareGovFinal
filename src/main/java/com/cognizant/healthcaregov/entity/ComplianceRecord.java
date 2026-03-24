package com.cognizant.healthcaregov.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "compliance_record")
public class ComplianceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer complianceID;

    @Column(nullable = false)
    private Integer entityId;

    @Column(nullable = false)
    private String type; // Appointment | Treatment | Hospital

    @Column(nullable = false)
    private String result; // Pass | Fail

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDate date;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
