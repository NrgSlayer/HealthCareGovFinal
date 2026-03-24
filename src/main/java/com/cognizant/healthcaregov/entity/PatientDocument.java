package com.cognizant.healthcaregov.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "patient_document")
public class PatientDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer documentID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patientID", nullable = false)
    private Patient patient;

    @Column(nullable = false)
    private String docType; // IDProof | HealthCard

    @Column(nullable = false)
    private String fileURI;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime uploadedDate;

    @Column(nullable = false)
    private String verificationStatus; // Pending | Verified | Rejected
}
