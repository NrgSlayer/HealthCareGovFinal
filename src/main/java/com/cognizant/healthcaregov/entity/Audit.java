package com.cognizant.healthcaregov.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "audit")
public class Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer auditID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officerID", nullable = false)
    private User officer;

    @Column(nullable = false)
    private String scope;

    @Column(columnDefinition = "TEXT")
    private String findings;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String status; // Scheduled | In-Progress | Completed
}
