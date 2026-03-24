package com.cognizant.healthcaregov.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "report")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reportID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospitalID", nullable = false)
    private Hospital hospital;

    @Column(nullable = false)
    private String scope; // Appointment | Treatment | Hospital | Compliance

    @Column(columnDefinition = "TEXT")
    private String metrics; // JSON string

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime generatedDate;
}
